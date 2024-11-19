package com.fpoly.nhom2.mifoodapp.model;

import java.util.List;

public class Order {
    private String orderId;
    private String user;
    private List<Cart> OrderDetails; // GioHang là lớp đại diện cho một sản phẩm
    private double totalAmount;
    private String phone;
    private String address;
    private String orderDate;
    private int status;
    private double tax;
    private String userID;
    private double deliveryFee;

    public String getUserID() {
        return userID;
    }

    public List<Cart> getOrderDetails() {
        return OrderDetails;
    }

    public void setOrderDetails(List<Cart> orderDetails) {
        OrderDetails = orderDetails;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public Order() {
        // Required empty constructor
    }

    // Getters and setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }



}
