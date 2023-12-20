package com.example.wallet.controller;

import com.example.wallet.model.dto.ApiResponse;
import com.example.wallet.model.dto.TransactionData;
import com.example.wallet.model.dto.User;
import com.example.wallet.repo.TransactionDataRepo;
import com.example.wallet.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RequestMapping("/wallet")
@RestController
public class UserController {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private TransactionDataRepo transactionDataRepo;

    @PostMapping("/register")
    public ApiResponse registerUser(@RequestBody User userReq)
    {
        if (userRepo.findByEmail(userReq.getEmail()).isPresent())
        {
            return new ApiResponse(HttpStatus.BAD_REQUEST, "Account exists with this email already");
        }

        User user = new User();
        user.setUsername(userReq.getUsername());
        user.setEmail(userReq.getEmail());
        user.setPassword(userReq.getPassword());
        user.setWalletBalance(userReq.getWalletBalance());

        userRepo.save(user);

        TransactionData transactionData = new TransactionData();
        transactionData.setTransaction(new ArrayList<>());
        transactionData.setUserId(user.getId());

        transactionDataRepo.save(transactionData);



        return new ApiResponse(HttpStatus.OK,"User created successfully");
    }

}
