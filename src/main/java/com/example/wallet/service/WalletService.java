package com.example.wallet.service;

import com.example.wallet.features.EmailFeature;
import com.example.wallet.features.JwtFeature;
import com.example.wallet.mapper.TransactionDataMapper;
import com.example.wallet.mapper.UserMapper;
import com.example.wallet.model.Transaction;
import com.example.wallet.model.dto.*;
import com.example.wallet.repo.TransactionDataRepo;
import com.example.wallet.repo.UserRepo;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class WalletService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private TransactionDataRepo transactionDataRepo;
    @Autowired
    private EmailFeature emailFeature;
    @Autowired
    private JwtFeature jwtFeature;

    private static final UserMapper USER_INFO_INSTANCE =
            Mappers.getMapper(UserMapper.class);

    private static final TransactionDataMapper TRANSACTION_INSTANCE =
            Mappers.getMapper(TransactionDataMapper.class);
    User user = new User();
    TransactionData transactionData = new TransactionData();

//    @Transactional
    public ApiResponse createUser(User userReq){
        if (userRepo.findByEmail(userReq.getEmail()).isPresent())
        {
            return new ApiResponse(HttpStatus.BAD_REQUEST, "Account exists with this email already");
        }

        String encryptedPassword = new BCryptPasswordEncoder().encode(userReq.getPassword());
        User user1 = User.builder()
                .id(UUID.randomUUID().toString())
                .username(userReq.getUsername())
                .email(userReq.getEmail())
                        .password(encryptedPassword)
                                .walletBalance(userReq.getWalletBalance()).build();
//        user.setId(UUID.randomUUID().toString());
//        user.setUsername(userReq.getUsername());
//        user.setEmail(userReq.getEmail());
//        user.setPassword(encryptedPassword);
//        user.setWalletBalance(userReq.getWalletBalance());

        userRepo.save(USER_INFO_INSTANCE.dtoToModel(user1));
//            userRepo.save(user);

//        transactionData.setTransaction(new ArrayList<>());
//        transactionData.setUserId(user.getId());

        TransactionData transactionData1 = TransactionData.builder()
                .id(UUID.randomUUID().toString())
                        .userId(user1.getId())
                                .transaction(new ArrayList<>()).build();

//        transactionDataRepo.save(transactionData);
        transactionDataRepo.save(TRANSACTION_INSTANCE.dtoToModel(transactionData1));

        emailFeature.sendEmail(userReq.getEmail());

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

        return jwtFeature.generateToken(user.getEmail());
    }

    public UserDataResponse data(UserReq userReq)
    {
        String token = userReq.getToken();
        if (jwtFeature.extractExpiration(token).before(new Date())) {
            throw new RuntimeException("Token is expired or invalid");
        }
        String username = jwtFeature.extractUsername(token);
        Optional<User> optionalUser = userRepo.findByEmail(username);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("Not authorized user");
        }
        user = optionalUser.get();
        user.setPassword(null);

        List<TransactionData> transactions = transactionDataRepo.findByUserId(user.getId());

        return new UserDataResponse(user, transactions);
    }
//    @Transactional
    public ApiResponse recharge(RechargeReq rechargeReq,String authHeader)
    {

        String username = jwtFeature.extractUsername(authHeader.substring(7));

        Optional<User> optionalUser = userRepo.findByEmail(username);

        if (optionalUser.isEmpty())
        {
//            throw new RuntimeException("Not authorized user");

            return new ApiResponse(HttpStatus.BAD_REQUEST, "Not authorized user");
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

        Transaction recharge = new Transaction(UUID.randomUUID().toString(),"Recharge", rechargeReq.getAmount(), rechargeReq.getEmail(),null, new Date());
        Transaction cashback = new Transaction(UUID.randomUUID().toString(), "Cashback", rechargeReq.getAmount() * 0.01, "Wallet@mail.com", null,new Date());

        TransactionData temp = userTransactions.get(0);

        List<Transaction> transactions = temp.getTransaction();
        transactions.add(recharge);
        transactions.add(cashback);
        temp.setTransaction(transactions);

//        transactionDataRepo.save(temp);
        transactionDataRepo.save(TRANSACTION_INSTANCE.dtoToModel(temp));
        userRepo.save(USER_INFO_INSTANCE.dtoToModel(user));
//        userRepo.save(user);

        return new ApiResponse(HttpStatus.OK, "Wallet recharged successfully with cashback");
    }

//    @Transactional
    public ApiResponse transfer(TransferReq transferReq, String authHeader) {

        String token = authHeader.substring(7);
        String username = jwtFeature.extractUsername(token);

        Optional<User> optionalFromUser = userRepo.findByEmail(username);
        Optional<User> optionalToUser = userRepo.findByEmail(transferReq.getToEmail());

        if (optionalFromUser.isEmpty() || optionalToUser.isEmpty()) {
            return new ApiResponse(HttpStatus.BAD_REQUEST, "User not found");
        }

        if (transferReq.getAmount() <= 0) {
            return new ApiResponse(HttpStatus.BAD_REQUEST, "Invalid amount");
        }

        if (username.equals(transferReq.getToEmail())) {
            return new ApiResponse(HttpStatus.BAD_REQUEST, "Cannot transfer funds to oneself");
        }

        User fromUser = optionalFromUser.get();
        User toUser = optionalToUser.get();

        if (fromUser.getWalletBalance() < transferReq.getAmount())
        {
            return new ApiResponse(HttpStatus.BAD_REQUEST, "Insufficient balance");
        }

        Transaction fromUserTransaction = new Transaction(UUID.randomUUID().toString(), "Debited", -transferReq.getAmount(), toUser.getEmail(),fromUser.getEmail(), new Date());
        Transaction toUserTransaction = new Transaction(UUID.randomUUID().toString(), "Credited", transferReq.getAmount(), fromUser.getEmail(),toUser.getEmail(), new Date());

        TransactionData fromUserTransactionList = transactionDataRepo.findByUserId(fromUser.getId()).stream().findFirst().orElse(null);
        if (fromUserTransactionList == null) {
            fromUserTransactionList = new TransactionData();
            fromUserTransactionList.setUserId(fromUser.getId());

            fromUserTransactionList.setTransaction(new ArrayList<>());
        }
        fromUserTransactionList.getTransaction().add(fromUserTransaction);
//        transactionDataRepo.save(fromUserTransactionList);
//        transactionDataRepo.save(TRANSACTION_INSTANCE.dtoToModel(fromUserTransactionList));

        TransactionData toUserTransactionList = transactionDataRepo.findByUserId(toUser.getId()).stream().findFirst().orElse(null);
        if (toUserTransactionList == null) {
            toUserTransactionList = new TransactionData();
            toUserTransactionList.setUserId(toUser.getId());
            toUserTransactionList.setTransaction(new ArrayList<>());
        }
        toUserTransactionList.getTransaction().add(toUserTransaction);
//        transactionDataRepo.save(toUserTransactionList);
//        transactionDataRepo.save(TRANSACTION_INSTANCE.dtoToModel(toUserTransactionList));
        optionalFromUser.get().setWalletBalance(fromUser.getWalletBalance() - transferReq.getAmount());
        optionalToUser.get().setWalletBalance(toUser.getWalletBalance() + transferReq.getAmount());

//        userRepo.save(USER_INFO_INSTANCE.dtoToModel(optionalFromUser.get()));
//        userRepo.save(USER_INFO_INSTANCE.dtoToModel(optionalToUser.get()));
//        userRepo.save(optionalFromUser.get());
//        userRepo.save(optionalToUser.get());

        return new ApiResponse(HttpStatus.OK, "Transfer successful");
    }
}
