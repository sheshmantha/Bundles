package com.shesh.api;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import com.shesh.model.Bundle;
import com.shesh.model.Cart;
import com.shesh.model.Item;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.Duration;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BundledPriceComputerTest {
    private static Bundle b1, b2, b3;
    private static Cart cart;
    private static ActorSystem actorSystem;
    private static List<Bundle> bundles;

    class PrivateBundleChecker {
        Bundle b;

        PrivateBundleChecker(Bundle b) {
            this.b = b;
        }

        Messages.MatchBundleResult match(Set<Item> set) {
            return new Messages.MatchBundleResult(b, set.containsAll(b.items));
        }
    }

    @BeforeClass
    public static void startup() {
        cart = new Cart();
        cart.items.add(new Item("a", 1, 1.50f));
        cart.items.add(new Item("b", 1, 3.50f));
        cart.items.add(new Item("c", 1, 4.50f));
//        cart.items.add(new Item("e", 1, 14.50f));

        b1 = new Bundle("Hello1");
        b1.items.add(new Item("a", 1, 1.50f));
        b1.items.add(new Item("c", 1, 4.50f));
        b1.setPrice(3.0f);
        b2 = new Bundle("Hello2");
        b2.items.add(new Item("b", 1, 3.50f));
        b2.items.add(new Item("c", 1, 4.50f));
        b2.setPrice(2.0f);
        b3 = new Bundle("Hello3");
        b3.items.add(new Item("d", 1, 4.50f));
        b3.items.add(new Item("e", 1, 14.50f));
        b3.setPrice(10.0f);
        bundles = Arrays.asList(b1, b2, b3);
        actorSystem = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        actorSystem.shutdown();
        actorSystem.awaitTermination(Duration.create("10 seconds"));
    }

    @Test
    public void testCart3() throws Exception {
        new JavaTestKit(actorSystem) {{
            final ActorRef bc = actorSystem.actorOf(BundledPriceComputer.props(), "BundlePriceComputer");
            bc.tell(new Messages.AddBundle(b1), getTestActor());
            bc.tell(new Messages.AddBundle(b2), getTestActor());
            bc.tell(new Messages.PriceCart(cart), getTestActor());
            final Messages.PriceCartResult result = expectMsgClass(Messages.PriceCartResult.class);
            new Within(duration("10 seconds")) {
                @Override
                protected void run() {
                    System.out.println("Cart price: " + result.totalPrice);
                    printBundles(" picked: ", result.picked);
                    assertEquals(b2, result.picked.iterator().next());
                    assertEquals(Double.valueOf(result.totalPrice), Double.valueOf(3.5f));
                }
            };
        }};
    }

    @Test
    public void testCartWOAddingBundles() throws Exception {
        new JavaTestKit(actorSystem) {{
            final ActorRef bc = actorSystem.actorOf(BundledPriceComputer.props(), "BundlePriceComputer");
//            bc.tell(new Messages.AddBundle(b1), getTestActor());
//            bc.tell(new Messages.AddBundle(b2), getTestActor());
            bc.tell(new Messages.PriceCart(cart), getTestActor());
            final Messages.PriceCartResult result = expectMsgClass(Messages.PriceCartResult.class);
            new Within(duration("10 seconds")) {
                @Override
                protected void run() {
                    System.out.println("Cart price: " + result.totalPrice);
                    assertEquals(0, result.picked.size());
                    assertEquals(Double.valueOf(result.totalPrice), Double.valueOf(9.5f));
                }
            };
        }};
    }

    @Test
    public void testFlatMap() throws Exception {
        List<Item> things = bundles.stream().flatMap(bundle -> bundle.items.stream())
                .collect(Collectors.toList());
        System.out.println(things);
    }

    @Test
    public void testFilter() throws Exception {
        List<Set<Item>> things = cart.getPowerSet().stream().filter(set -> set.size() > 1).collect(Collectors.toList());
        things.stream().forEach(set -> {
            System.out.print("[ ");
            set.forEach(item -> System.out.print(item.toString() + ", "));
            System.out.println(" ]");
        });
        assert things.size() == 4;
    }

    @Test
    public void testPartitioning() throws Exception {
        List<Set<Item>> things = cart.getPowerSet().stream().filter(set -> set.size() > 1).collect(Collectors.toList());
        assert things.size() == 4;
        things.forEach(set -> {
            System.out.print("Finding Bundles for subset: [ ");
            set.forEach(item -> System.out.print(item.toString() + ", "));
            System.out.println(" ]");
            Map<Boolean, List<Bundle>> matches = bundles.parallelStream().collect(Collectors.partitioningBy(
                    bundle -> set.containsAll(bundle.items)
            ));
            matches.get(Boolean.TRUE).forEach(bundle -> System.out.println(bundle));
        });

    }

    @Test
    public void testPartitioningByBundleCopy() throws Exception {
        List<Set<Item>> things = cart.getPowerSet().stream().filter(set -> set.size() > 1).collect(Collectors.toList());
        assert things.size() == 4;
        List<Bundle> bundleDB = new ArrayList<Bundle>(bundles);
        Set<Bundle> matchedBundles = new HashSet<>();
//        printBundles("All bundles", bundleDB);
        things.forEach(set -> {
            System.out.print("Finding Bundles for subset: [ ");
            set.forEach(item -> System.out.print(item.toString() + ", "));
            System.out.println(" ]");
            Map<Boolean, List<Bundle>> matches = bundleDB.parallelStream().collect(Collectors.partitioningBy(
                    bundle -> set.containsAll(bundle.items)
            ));
            matches.get(Boolean.TRUE).forEach(bundle -> {
//                System.out.println(bundle);
                bundleDB.remove(bundle);
                matchedBundles.add(bundle);
//                printBundles("Matched", bundleDB);
            });
        });
        printBundles("Final matches", matchedBundles);
    }

    @Test
    public void testPartitioning2() throws Exception {
        Set<Bundle> matched = new HashSet<Bundle>();
        cart.getPowerSet().stream()
                .filter(set -> set.size() > 1)
                .forEach(set -> {
                    matched.addAll(bundles.parallelStream()
                            .collect(Collectors.partitioningBy(bundle -> set.containsAll(bundle.items)))
                            .get(Boolean.TRUE));
                });
        assert matched.size() == 2;
        assert matched.containsAll(Arrays.asList(b2, b1));
        assertFalse(hasNoIntersection(matched));
        printBundles("matched", matched);
        Optional<Bundle> cheapest = matched.parallelStream().min((bun1, bun2) -> Double.compare(bun1.getPrice(), bun2.getPrice()));
        if (cheapest.isPresent()) {
            System.out.println("Cheapest: " + cheapest.get());
            Set<Item> remaining = new HashSet<Item>(cart.items);
            remaining.removeAll(cheapest.get().items);
            remaining.forEach(item -> System.out.println(item));
        } else
            System.out.println("Cheapest not found");
    }

    private Boolean hasNoIntersection(Set<Bundle> set) {
        Map<Item, Long> counts = set.stream().flatMap(bundle -> bundle.items.stream()).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return (counts.values().stream().filter(c -> c > 1).count()) == 0;
    }

    private void printBundles(String desc, Collection<Bundle> coll) {
        System.out.println(desc + ">>>");
        coll.forEach(bundle -> System.out.println(bundle));
        System.out.println("<<<" + desc);
    }
}