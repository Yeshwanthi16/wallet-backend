package com.example.wallet.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
//@Document(collection = "Users")
@Builder
public class User {
//    @Id
    private String id;

    private String username;

    private String password;

    private String email;

    @Builder.Default
    private Double walletBalance = 0.0;

}

