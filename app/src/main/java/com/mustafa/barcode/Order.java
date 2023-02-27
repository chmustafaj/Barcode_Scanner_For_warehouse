package com.mustafa.barcode;

import android.util.Pair;

import java.util.ArrayList;

public class Order {
    private String orderCode;
    private int noOfProducts, totalQuantity;
    private ArrayList<Pair<String,Integer>> productList;

    public Order() {

    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public int getNoOfProducts() {
        return noOfProducts;
    }

    public void setNoOfProducts(int noOfProducts) {
        this.noOfProducts = noOfProducts;
    }

    public ArrayList<Pair<String, Integer>> getProductList() {
        return productList;
    }

    public void setProductList(ArrayList<Pair<String, Integer>> productList) {
        this.productList = productList;
    }

    public Order(String orderCode, int totalQuantity, int noOfProducts, ArrayList<Pair<String, Integer>> productList) {
        this.orderCode = orderCode;
        this.totalQuantity = totalQuantity;
        this.noOfProducts = noOfProducts;
        this.productList = productList;
    }
}
