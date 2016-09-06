package com.shesh.api;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.dispatch.Mapper;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.shesh.model.Bundle;
import com.shesh.model.Cart;
import com.shesh.model.Item;
import scala.concurrent.Future;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static akka.dispatch.Futures.future;
import static akka.dispatch.Futures.sequence;

public class BundledPriceComputer extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(context().system(), this);
    private final List<Bundle> bundles = new ArrayList<Bundle>();
    private List<ActorRef> actors = new ArrayList<ActorRef>();

    static Props props() {
        return Props.create(BundledPriceComputer.class, () -> new BundledPriceComputer());
    }

    BundledPriceComputer() {
        receive(ReceiveBuilder
                        .match(Messages.PriceCart.class, priceCart -> {
                            sender().tell(process(priceCart.cart), self());
                        })
                        .match(Messages.AddBundle.class, addBundle -> {
                            bundles.add(addBundle.bundle);
                            actors.add(context().system().actorOf(BundleChecker.props(addBundle.bundle), addBundle.bundle.name + "-actor"));
                        })
                        .build()
        );
    }

    private Boolean hasNoIntersection(Set<Bundle> set) {
        Map<Item, Long> counts = set.stream().flatMap(bundle -> bundle.items.stream()).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return (counts.values().stream().filter(c -> c > 1).count()) == 0;
    }

    private Messages.PriceCartResult process(Cart cart) {
        Set<Bundle> matched = new HashSet<Bundle>();
        Messages.PriceCartResult result = new Messages.PriceCartResult();
        result.cart = cart;
        cart.getPowerSet().stream()
                .filter(set -> set.size() > 1)
                .forEach(set -> {
                    matched.addAll(bundles.parallelStream()
                            .collect(Collectors.partitioningBy(bundle -> set.containsAll(bundle.items)))
                            .get(Boolean.TRUE));
                });
        if (!hasNoIntersection(matched)) {
            /* some bundles apply */
            Optional<Bundle> cheapest = matched.parallelStream().min((bun1, bun2) -> Double.compare(bun1.getPrice(), bun2.getPrice()));
            result.picked = new HashSet<Bundle>();
            if (cheapest.isPresent()) {
                log.debug("Cheapest: " + cheapest.get());
                /* TODO: find if there are any matches for remaining in matched -- send remaining as cart to self() ?? */
                result.picked.add(cheapest.get());
            } else {
                log.debug("Cheapest not found...picking first bundle.");
                if (matched.size() > 0) /* TODO: I don't like this magic... */
                    result.picked.add(matched.iterator().next());
            }
            Set<Item> remaining = new HashSet<Item>(cart.items);
            result.picked.forEach(bundle -> remaining.removeAll(bundle.items));
            double sum = remaining.parallelStream().mapToDouble(item -> item.getPrice()).sum();
            result.totalPrice = result.picked.parallelStream().mapToDouble(item -> item.getPrice()).sum() + sum;
        } else {
            /* all bundles apply... unless there are none ;-) */
            result.picked = matched;
            if (matched.size() == 0)
                result.totalPrice = cart.items.parallelStream().mapToDouble(item -> item.getPrice()).sum();
            else
                result.totalPrice = result.picked.parallelStream().mapToDouble(item -> item.getPrice()).sum();
        }
        return result;
    }


    private void processByFutures(Cart cart) {
        final List<Future<Messages.MatchBundleResult>> futures = new ArrayList<Future<Messages.MatchBundleResult>>();
        cart.getPowerSet()
                .stream().filter(set -> set.size() > 1).forEach(set -> {
                    bundles.parallelStream().forEach(bundle -> {
                        futures.add(future(() -> {
                            return new Messages.MatchBundleResult(bundle, set.containsAll(bundle.items));
                        }, context().dispatcher()));
                    });
                    Future<Iterable<Messages.MatchBundleResult>> futureList = sequence(futures, context().dispatcher());
                    /* Filter out bundles that don't match and pick smallest bundle */
/*                    Future<List<Bundle>> matched = futureList.map((iter) -> {
                        List<Bundle> matches = new ArrayList<Bundle>();
                        for (Messages.MatchBundleResult matchBundleResult : iter) {
                            if (matchBundleResult.matched)
                                matches.add(matchBundleResult.bundle);
                        }
                        return matches;
                    }, context().dispatcher());*/


                    Future<List<Bundle>> matchedBundles = futureList.map(new Mapper<Iterable<Messages.MatchBundleResult>, List<Bundle>>() {
                        public List<Bundle> apply(Iterable<Messages.MatchBundleResult> iter) {
                            List<Bundle> matches = new ArrayList<Bundle>();
                            for (Messages.MatchBundleResult mbr : iter) {
                                if (mbr.matched) {
                                    matches.add(mbr.bundle);
                                }
                            }
                            return matches;
                        }
                    }, context().dispatcher());


                }
        );

    }
}
