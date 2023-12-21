package com.example.wallet.controller;

import com.example.wallet.features.JwtFeature;
import com.example.wallet.model.dto.*;
import com.example.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;


@RequestMapping("/wallet")
@RestController
public class UserController {
    @Autowired
    WalletService walletService;

    @PostMapping("/register")
    public ApiResponse registerUser(@RequestBody User userReq)
    {
        return walletService.createUser(userReq);
    }

    @PostMapping("/login")
    public String loginUser(@RequestBody LoginReq loginReq)
    {
       return walletService.login(loginReq);
    }

    @PostMapping("/data")
    public UserDataResponse userData(@RequestBody UserReq userReq) {return walletService.data(userReq);}

    @PostMapping("/recharge")
    public ApiResponse recharge(@RequestBody RechargeReq rechargeReq,@RequestHeader("Authorization") String authHeader){ return walletService.recharge(rechargeReq, authHeader);}

    @PostMapping("/transfer")
    public ApiResponse transferAmount(@RequestBody TransferReq transferReq, @RequestHeader("Authorization") String authHeader) { return walletService.transfer(transferReq,authHeader);}
}
