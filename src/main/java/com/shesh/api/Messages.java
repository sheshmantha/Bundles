package com.shesh.api;

import com.shesh.model.Bundle;
import com.shesh.model.Cart;
import com.shesh.model.Item;

import java.util.List;
import java.util.Set;

public class Messages {
    public static class AddBundle {
        public Bundle bundle;
        public AddBundle(Bundle b) { this.bundle = b; }
    }
    public static class MatchBundle {
        public Set<Item> set;
        public MatchBundle(Set<Item> set) { this.set = set; }
    }
    public static class MatchBundleResult {
        public Bundle bundle;
        public boolean matched = false;
        public MatchBundleResult(Bundle b, boolean foundMatch) { this.bundle = b; matched = foundMatch; }
    }

    public static class PriceCart {
        public Cart cart;

        public PriceCart(Cart c) {
            cart = c;
        }
    }

    /* Arguably this can be a heavy object and result in expensive serialization 'tween actors -- leave for future optimization */
    public static class PriceCartResult {
        public Cart cart;
        public Set<Bundle> matches;
        public Set<Bundle> picked;
        public double totalPrice;
    }
}
