package com.example.wallet.controller;

import com.example.wallet.model.dto.*;
import com.example.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/wallet")
@RestController
@CrossOrigin
public class UserController {

    private final WalletService walletService;
//    @Autowired
    public UserController(WalletService walletService) {
        this.walletService = walletService;
    }

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
