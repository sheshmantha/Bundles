package com.shesh.model;

import org.junit.Test;

import java.util.Set;

public class CartTest {
    @Test
    public void getPowerSet() throws Exception {
        Cart cart = new Cart();
        cart.items.add(new Item("a"));
        cart.items.add(new Item("b"));
        cart.items.add(new Item("c"));
        Set<Set<Item>> ps = cart.getPowerSet();
        ps.stream().forEach(set -> {
            System.out.print("(");
            for (Item item :
                    set) {
                System.out.print(item + ", ");
            }
            System.out.println("}");
        });
        assert ps.size() == 8;
    }
}