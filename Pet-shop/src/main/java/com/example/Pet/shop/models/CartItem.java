package com.example.Pet.shop.models;

public class CartItem {
    private Product product;
    private int quality;

    public CartItem(Product product, int quality) {
        this.product = product;
        this.quality = quality;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }
}
