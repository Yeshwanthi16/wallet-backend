package com.example.wallet.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Data
@Document(collection = "Users")
public class UserInfoEntityImpl implements UserInfoEntity {
    @Id
    String id;

    String username;

    String password;

    String email;

    Double walletBalance = 0.0;
}
