package com.shesh.model;

public class Item {
    String product; /* Can be a first class object; use String for simplicity */
    int qty;
    float price;

    public Item(String name) {
        this(name, 1, 2.50f);
    }

    public Item(String name, int q, float p) {
        product = name;
        qty = q;
        price = p;
    }

    @Override
    public String toString() {
        return "Item{" +
                "product='" + product + '\'' +
                ", qty=" + qty +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        if (qty != item.qty) return false;
        return product != null ? product.equals(item.product) : item.product == null;

    }

    @Override
    public int hashCode() {
        int result = product != null ? product.hashCode() : 0;
        result = 31 * result + qty;
        return result;
    }
}
