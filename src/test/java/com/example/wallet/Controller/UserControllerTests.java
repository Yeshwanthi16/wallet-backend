package com.example.wallet.Controller;

import com.example.wallet.controller.UserController;
import com.example.wallet.model.Transaction;
import com.example.wallet.model.dto.*;
import com.example.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class UserControllerTests {

    @Mock
    private WalletService walletService;

    @InjectMocks
    private UserController userController;

    @Test
    void testRegisterUser_Successful() {
        User userReq = new User();
        userReq.setPassword("1234");
        userReq.setEmail("Test@gmail.com");
        userReq.setUsername("Test");
        ApiResponse expectedResponse = new ApiResponse(HttpStatus.OK, "User registered successfully");

        when(walletService.createUser(userReq)).thenReturn(expectedResponse);

        ApiResponse actualResponse = userController.registerUser(userReq);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void testLoginUser_Successful() {
        LoginReq loginReq = new LoginReq();
        loginReq.setEmail("Test@gmail.com");
        loginReq.setPassword("1234");
        String expectedResponse = "jwt-token";

        when(walletService.login(loginReq)).thenReturn(expectedResponse);

        String actualResponse = userController.loginUser(loginReq);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void testUserData_Successful() {
        User user = new User();
        user.setPassword("1234");
        user.setEmail("Test@gmail.com");
        user.setUsername("Test");
        UserReq userReq = new UserReq();
        userReq.setToken("Token");
        Transaction transactionData = new Transaction();
        transactionData.setAmount(90.0);
        transactionData.setToEmail("Test1@gmail.com");
        transactionData.setFromEmail("Test@gmail.com");
        transactionData.setType("Debited");
        UserDataResponse expectedResponse = new UserDataResponse();
        expectedResponse.setUser(user);
        expectedResponse.setTransactions(List.of(new TransactionData("123", "456", List.of(transactionData))));

        when(walletService.data(userReq)).thenReturn(expectedResponse);

        UserDataResponse actualResponse = userController.userData(userReq);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void testRecharge_Successful() {
        RechargeReq rechargeReq = new RechargeReq();
        rechargeReq.setEmail("test@gmail.com");
        rechargeReq.setAmount(90.0);
        String authHeader = "Bearer jwt-token";
        ApiResponse expectedResponse = new ApiResponse(HttpStatus.OK, "Wallet recharged successfully");

        when(walletService.recharge(rechargeReq, authHeader)).thenReturn(expectedResponse);

        ApiResponse actualResponse = userController.recharge(rechargeReq, authHeader);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void testTransferAmount_Successful() {
        TransferReq transferReq = new TransferReq();
        transferReq.setFromEmail("test@gmail.com");
        transferReq.setAmount(90.0);
        transferReq.setToEmail("test1@gmail.com");
        String authHeader = "Bearer jwt-token";
        ApiResponse expectedResponse = new ApiResponse(HttpStatus.OK, "Transfer successful");

        when(walletService.transfer(transferReq, authHeader)).thenReturn(expectedResponse);

        ApiResponse actualResponse = userController.transferAmount(transferReq, authHeader);

        assertEquals(expectedResponse, actualResponse);
    }
}
