package com.shesh.model;

import java.util.HashSet;
import java.util.Set;

public class Bundle {
    public Set<Item> items;

    private float price;

    public Bundle() {
        items = new HashSet<Item>();
        price = Integer.MAX_VALUE;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Bundle{" +
                "items={" + items +
                "}, price=" + price +
                '}';
    }
}
