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

public class BundleCheckerTest {
    private static Bundle myBundle;
    private static Cart cart;
    private static ActorSystem actorSystem;

    @BeforeClass
    public static void startup() {
        cart = new Cart();
        cart.items.add(new Item("a", 1, 1.50f));
        cart.items.add(new Item("b", 1, 3.50f));
        cart.items.add(new Item("c", 1, 4.50f));

        myBundle = new Bundle();
        myBundle.items.add(new Item("a"));
        myBundle.items.add(new Item("c"));
        myBundle.setPrice(3.0f);
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
            final ActorRef bc = actorSystem.actorOf(BundleChecker.props(myBundle), "bundleChecker1");
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

}