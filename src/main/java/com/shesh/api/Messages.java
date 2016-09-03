package com.shesh.api;

import com.shesh.model.Bundle;
import com.shesh.model.Item;

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
}
