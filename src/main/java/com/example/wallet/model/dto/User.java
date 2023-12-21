package com.example.wallet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@NoArgsConstructor
@Data
@AllArgsConstructor
@Document(collection = "Users")
public class User {
    @Id
    private String id;

    private String username;

    private String password;

    private String email;

    private Double walletBalance = 0.0;

}

