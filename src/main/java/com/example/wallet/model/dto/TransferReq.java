package com.example.wallet.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class TransferReq {

    private String fromEmail;

    private String toEmail;

    private double amount;
}
