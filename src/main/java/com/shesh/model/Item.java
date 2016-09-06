package com.shesh.model;

public class Item {
    String product; /* Can be a first class object; use String for simplicity */
    int qty;
    double price;

    public Item(String name) {
        this(name, 1, 1.50d);
    }

    public Item(String name, int q) {
        this(name, q, 1.50d);
    }

    public Item(String name, int q, double p) {
        product = name;
        qty = q;
        price = p;
    }

    @Override
    public String toString() {
        return "Item{" +
                "product='" + product + '\'' +
                ", qty=" + qty +
                ", price=" + price +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        if (Double.compare(item.price, price) != 0) return false;
        return product != null ? product.equals(item.product) : item.product == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = product != null ? product.hashCode() : 0;
        temp = Double.doubleToLongBits(price);
        result = 31 * result + qty + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public double getPrice() {
        return price;
    }
}
