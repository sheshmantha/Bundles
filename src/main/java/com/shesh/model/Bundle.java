package com.shesh.model;

import java.util.HashSet;
import java.util.Set;

public class Bundle {
    public String name;
    public Set<Item> items;

    private double price;

    public Bundle(String a) {
        name = a;
        items = new HashSet<Item>();
        price = Integer.MAX_VALUE;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPrice() {
        return this.price;
    }

    @Override
    public String toString() {
        return "Bundle:" + name + " {" +
                "items={" + items +
                "}, price=" + price +
                '}';
    }
}
