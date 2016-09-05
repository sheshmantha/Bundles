package com.shesh.api;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.shesh.model.Bundle;

public class BundleChecker extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    static Props props(Bundle b) {
        return Props.create(BundleChecker.class, () -> new BundleChecker(b));
    }
    private final Bundle myBundle;

    BundleChecker(Bundle b) {
        this.myBundle = b;
        receive(ReceiveBuilder
                .match(Messages.MatchBundle.class, cb -> {
                   cb.set.stream().forEach(item -> {
                       System.out.println(item);
                   });
                    sender().tell(new Messages.MatchBundleResult(myBundle, cb.set.containsAll(myBundle.items)), self());
                })
                .build()
        );
    }
/*    PartialFunction<Object, BoxedUnit> initialize = ReceiveBuilder.match(InitBundle.class, t -> {
        myBundle = t.bundle;
        context().become(available);
    }).build();

    PartialFunction<Object, BoxedUnit> available = ReceiveBuilder.match(MatchBundle.class, )

    public BundleChecker() { receive(initialize); } */
}
