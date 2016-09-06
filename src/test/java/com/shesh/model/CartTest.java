package com.shesh.model;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Set;

public class CartTest {
    private static Cart cart;

    @BeforeClass
    public static void startup() {
        cart = new Cart();
        cart.items.add(new Item("a", 1, 1.50f));
        cart.items.add(new Item("b", 1, 3.50f));
        cart.items.add(new Item("c", 1, 4.50f));
    }

    @Test
    public void getPowerSet3() throws Exception {
        Set<Set<Item>> ps = cart.getPowerSet();
        printPS(ps);
        assert ps.size() == 8;
    }

    @Test
    public void getPowerSet4() throws Exception {
        cart.items.add(new Item("e", 1, 14.50f));
        Set<Set<Item>> ps = cart.getPowerSet();
        printPS(ps);
        assert ps.size() == 16;
    }

    private void printPS(Set<Set<Item>> ps) {
        ps.stream().forEach(set -> {
            System.out.print("(");
            for (Item item :
                    set) {
                System.out.print(item + ", ");
            }
            System.out.println("}");
        });
    }
}