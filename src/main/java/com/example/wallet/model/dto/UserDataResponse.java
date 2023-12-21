package com.example.wallet.model.dto;

import java.util.List;

public class UserDataResponse {
    private User user;
    private List<TransactionData> transactions;

    public UserDataResponse(User user, List<TransactionData> transactions) {
        this.user = user;
        this.transactions = transactions;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<TransactionData> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionData> transactions) {
        this.transactions = transactions;
    }


}
