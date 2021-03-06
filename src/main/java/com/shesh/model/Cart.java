package com.shesh.model;

import java.util.*;

public class Cart {
    public Set<Item> items;
    private Set<Set<Item>> ps;

    public Cart() {
        items = new HashSet<Item>();
        ps = null;
    }

    public void add(String product, int qty) {
      items.add(new Item(product, qty));
    }

    public Set<Set<Item>> getPowerSet() {
        if (ps == null) {
            long num = 1L << items.size();
            ps = powerset(new ArrayList<Item>(items), num);
        }
        return ps;
    }

    private Set<Set<Item>> powerset(List<Item> list, long num) {
        if (num < 0)
            return new HashSet<Set<Item>>();
        Set<Item> currSubset = new HashSet<Item>();
        for (int idx = 0; idx < list.size(); idx++) {
            if ((num & (1L << idx)) != 0)
                currSubset.add(list.get(idx));
        }
        Set<Set<Item>> subsets = powerset(list, num-1);
        subsets.add(currSubset);
        return subsets;
    }

}