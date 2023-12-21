package com.example.wallet.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@NoArgsConstructor
@Data
@AllArgsConstructor
public class Transaction {

    private String id;

    private String type;

    private Double amount;

    private String email;

    private Date date;
}


