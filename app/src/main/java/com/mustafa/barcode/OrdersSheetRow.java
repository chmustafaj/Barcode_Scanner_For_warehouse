package com.mustafa.barcode;

public class OrdersSheetRow {
    private String orderCode, productCode, productQuantity;
    int index;
    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(String productQuantity) {
        this.productQuantity = productQuantity;
    }


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public OrdersSheetRow(String orderCode, String productCode, String productQuantity, int index) {
        this.orderCode = orderCode;
        this.productCode = productCode;
        this.productQuantity = productQuantity;
        this.index = index;
    }
}
