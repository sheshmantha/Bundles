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
import scala.concurrent.Future;

import java.util.ArrayList;
import java.util.List;

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
                            processByFutures(priceCart.cart);
                        })
                        .match(Messages.AddBundle.class, addBundle -> {
//                    bundles.add(addBundle.bundle);
                            actors.add(context().system().actorOf(BundleChecker.props(addBundle.bundle), addBundle.bundle.name + "-actor"));
                        })
                        .build()
        );
    }

    /*private void process(Cart cart) {
        cart.getPowerSet().stream().filter(set -> set.size() > 1)
                .forEach(set -> );
    }*/

    private void processByFutures(Cart cart) {
        final List<Future<Messages.MatchBundleResult>> futures = new ArrayList<Future<Messages.MatchBundleResult>>();
        cart.getPowerSet()
                .stream().forEach(set -> {
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
//  FIX-ME                              matches.stream().flatMap(
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
