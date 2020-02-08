package com.wfy.common;

import java.io.Serializable;

public class Order implements Serializable {
    int orderId = 99;


    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                '}';
    }
}
