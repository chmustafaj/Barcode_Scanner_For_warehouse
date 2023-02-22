package com.mustafa.barcode;

public class Product {
    private String code, description, location;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Product(String code, String description, String location) {
        this.code = code;
        this.description = description;
        this.location = location;
    }
}
