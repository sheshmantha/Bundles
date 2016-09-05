package com.shesh.api;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import com.shesh.model.Bundle;
import com.shesh.model.Cart;
import com.shesh.model.Item;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.Duration;

import java.util.*;
import java.util.stream.Collectors;

public class BundleCheckerTest {
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
    public void testSimpleMatch() throws Exception {
        new JavaTestKit(actorSystem) {{
            final ActorRef bc = actorSystem.actorOf(BundleChecker.props(b1), "bundleChecker1");
            bc.tell(new Messages.MatchBundle(cart.items), getTestActor());
            final Messages.MatchBundleResult result = expectMsgClass(Messages.MatchBundleResult.class);
            new Within(duration("10 seconds")) {
                @Override
                protected void run() {
                    System.out.println("Bundle: " + result.bundle + " match: " + result.matched);
                    Assert.assertTrue(result.matched);
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
        printBundles("All bundles", bundleDB);
        things.forEach(set -> {
            System.out.print("Finding Bundles for subset: [ ");
            set.forEach(item -> System.out.print(item.toString() + ", "));
            System.out.println(" ]");
            Map<Boolean, List<Bundle>> matches = bundleDB.parallelStream().collect(Collectors.partitioningBy(
                    bundle -> set.containsAll(bundle.items)
            ));
            matches.get(Boolean.TRUE).forEach(bundle -> {
                System.out.println(bundle);
                bundleDB.remove(bundle);
                printBundles("Matched", bundleDB);
            });
        });

    }

    private void printBundles(String desc, List<Bundle> blist) {
        System.out.println(desc + ">>>");
        blist.forEach(bundle -> System.out.println(bundle));
        System.out.println("<<<" + desc);
    }
}