package com.mustafa.barcode;

public class Order {
    private String orderCode, ProductCode, quantity;

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getProductCode() {
        return ProductCode;
    }

    public void setProductCode(String productCode) {
        ProductCode = productCode;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public Order(String orderCode, String productCode, String quantity) {
        this.orderCode = orderCode;
        ProductCode = productCode;
        this.quantity = quantity;
    }
}
