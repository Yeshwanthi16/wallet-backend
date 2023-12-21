package com.example.wallet.model.dto;

public class TransferReq {
    private String fromEmail;
    private String toEmail;
    private double amount;

    public TransferReq(String fromEmail, String toEmail, double amount) {
        this.fromEmail = fromEmail;
        this.toEmail = toEmail;
        this.amount = amount;
    }
    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
