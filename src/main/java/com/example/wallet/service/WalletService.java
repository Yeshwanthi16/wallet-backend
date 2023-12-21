package com.example.wallet.service;

import com.example.wallet.features.JwtFeature;
import com.example.wallet.model.Transaction;
import com.example.wallet.model.dto.*;
import com.example.wallet.repo.TransactionDataRepo;
import com.example.wallet.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;

@Service
public class WalletService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private TransactionDataRepo transactionDataRepo;
    @Autowired
    private JwtFeature jwtFeature;
    User user = new User();
    TransactionData transactionData = new TransactionData();

    public ApiResponse createUser(User userReq){
        if (userRepo.findByEmail(userReq.getEmail()).isPresent())
        {
            return new ApiResponse(HttpStatus.BAD_REQUEST, "Account exists with this email already");
        }

        user.setId(UUID.randomUUID().toString());
        user.setUsername(userReq.getUsername());
        user.setEmail(userReq.getEmail());
        user.setPassword(userReq.getPassword());
        user.setWalletBalance(userReq.getWalletBalance());

        userRepo.save(user);

        transactionData.setTransaction(new ArrayList<>());
        transactionData.setUserId(user.getId());

        transactionDataRepo.save(transactionData);

        return new ApiResponse(HttpStatus.OK,"User created successfully");
    }

    public String login(LoginReq loginReq)
    {
        Optional<User> optionalUser = userRepo.findByEmail(loginReq.getEmail());

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }

        user = optionalUser.get();

        if (!new BCryptPasswordEncoder().matches(loginReq.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        return JwtFeature.generateToken(user.getEmail());
    }

    public UserDataResponse data(UserReq userReq)
    {
        String token = userReq.getToken();
        if (JwtFeature.extractExpiration(token).before(new Date())) {
            throw new RuntimeException("Token is expired or invalid");
        }
        String username = JwtFeature.extractUsername(token);
        Optional<User> optionalUser = userRepo.findByEmail(username);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("Not authorized user");
        }
        user = optionalUser.get();
        user.setPassword(null);

        List<TransactionData> transactions = transactionDataRepo.findByUserId(user.getId());

        return new UserDataResponse(user, transactions);
    }

    public ApiResponse recharge(RechargeReq rechargeReq,String authHeader)
    {
        if (!authHeader.startsWith("Bearer "))
        {
            return new ApiResponse(HttpStatus.BAD_REQUEST, "Unauthorized");
        }

        String username = JwtFeature.extractUsername(authHeader.substring(7));

        Optional<User> optionalUser = userRepo.findByEmail(username);

        if (optionalUser.isEmpty())
        {
            throw new RuntimeException("Not authorized user");
        }

        if (rechargeReq.getAmount() <= 0)
        {
            return new ApiResponse(HttpStatus.BAD_REQUEST, "Invalid amount");
        }

        user = optionalUser.get();
        user.setWalletBalance( user.getWalletBalance() + rechargeReq.getAmount() + rechargeReq.getAmount() * 0.01 );

        List<TransactionData> userTransactions = transactionDataRepo.findByUserId(user.getId());

        if (userTransactions.isEmpty())
        {
            transactionData = new TransactionData();
            transactionData.setUserId(user.getId());
            transactionData.setTransaction(new ArrayList<>());
            userTransactions.add(transactionData);
        }

        Transaction recharge = new Transaction(UUID.randomUUID().toString(),"Recharge", rechargeReq.getAmount(), rechargeReq.getEmail(), new Date());
        Transaction cashback = new Transaction(UUID.randomUUID().toString(), "Cashback", rechargeReq.getAmount() * 0.01, rechargeReq.getEmail(), new Date());

        TransactionData transactionData = userTransactions.getFirst();

        List<Transaction> transactions = transactionData.getTransaction();
        transactions.add(recharge);
        transactions.add(cashback);
        transactionData.setTransaction(transactions);

        transactionDataRepo.save(transactionData);
        userRepo.save(user);

        return new ApiResponse(HttpStatus.OK, "Wallet recharged successfully with cashback");
    }

    public ApiResponse transfer(TransferReq transferReq, String authHeader) {
        return null;
    }
}
