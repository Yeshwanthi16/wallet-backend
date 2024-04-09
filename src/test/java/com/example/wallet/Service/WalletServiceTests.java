package com.example.wallet.Service;

import com.example.wallet.entity.TransactionDataImpl;
import com.example.wallet.entity.UserInfoEntityImpl;
import com.example.wallet.features.EmailFeature;
import com.example.wallet.features.JwtFeature;
import com.example.wallet.model.dto.*;
import com.example.wallet.repo.TransactionDataRepo;
import com.example.wallet.repo.UserRepo;
import com.example.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
//@WebMvcTest(UserController.class)
public class WalletServiceTests {

    @InjectMocks
    private WalletService walletservice;

    @Mock
    private User user;

    @Mock
    private UserRepo userRepo;

    @Mock
    private JwtFeature jwtFeature;

    @Mock
    private EmailFeature emailService;

    @Mock
    private TransactionDataRepo transactionListRepository;



    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterUserSuccess() {
        User registrationRequest = new User();
        registrationRequest.setUsername("johndoe");
        registrationRequest.setEmail("johndoe@example.com");
        registrationRequest.setPassword("password");

        when(userRepo.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepo.save(any(UserInfoEntityImpl.class))).thenReturn(new UserInfoEntityImpl());
        when(transactionListRepository.save(any(TransactionDataImpl.class))).thenReturn(new TransactionDataImpl());
        doNothing().when(emailService).sendEmail("johndoe@example.com");

        ApiResponse response = walletservice.createUser(registrationRequest);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("User created successfully", response.getResponse());

//        ArgumentCaptor<UserInfoEntityImpl> userCaptor = ArgumentCaptor.forClass(UserInfoEntityImpl.class);
//        verify(userRepo).save(userCaptor.capture());
//        UserInfoEntityImpl savedUser = userCaptor.getValue();
//        assertNotNull(savedUser.getId());
//        assertEquals(registrationRequest.getUsername(), savedUser.getUsername());
//        assertNotEquals(registrationRequest.getPassword(), savedUser.getPassword());
//        assertTrue(new BCryptPasswordEncoder().matches(registrationRequest.getPassword(), savedUser.getPassword()));
//        assertEquals(registrationRequest.getEmail(), savedUser.getEmail());
//        assertEquals(0.0, savedUser.getWalletBalance(), 0.0);
//
//        ArgumentCaptor<TransactionData> transactionListCaptor = ArgumentCaptor.forClass(TransactionData.class);
//        verify(transactionListRepository).save(transactionListCaptor.capture());
//        TransactionData savedTransactionList = transactionListCaptor.getValue();
//        assertNotNull(savedTransactionList.getUserId());
//        assertEquals(savedUser.getId(), savedTransactionList.getUserId());
//        assertTrue(savedTransactionList.getTransaction().isEmpty());
//
//        ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
//        verify(emailService).sendEmail(toCaptor.capture());
//        assertEquals(registrationRequest.getEmail(), toCaptor.getValue());
    }

    @Test
    public void testRegisterUserAccountExists() {
        User registrationRequest = new User();
        registrationRequest.setEmail("test@gmail.com");
        registrationRequest.setPassword("1234");

        when(userRepo.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.of(new User()));

        ApiResponse response = walletservice.createUser(registrationRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals("Account exists with this email already", response.getResponse());

        verify(userRepo, never()).save(any(UserInfoEntityImpl.class));
        verify(transactionListRepository, never()).save(any(TransactionDataImpl.class));
        verify(emailService, never()).sendEmail(anyString());
    }

    @Test
    public void loginUser_ValidUser_ReturnsToken() {
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setPassword(new BCryptPasswordEncoder().encode("1234"));

        when(userRepo.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        when(jwtFeature.generateToken("test@gmail.com")).thenReturn("test_token");

        LoginReq LoginReq = new LoginReq();
        LoginReq.setEmail("test@gmail.com");
        LoginReq.setPassword("1234");

        String result = walletservice.login(LoginReq);

        assertEquals("test_token", result);
    }

    @Test
    public void loginUser_InvalidUser_ThrowsRuntimeException() {
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.empty());

        LoginReq LoginReq = new LoginReq();
        LoginReq.setEmail("test@gmail.com");
        LoginReq.setPassword("password");

        assertThrows(RuntimeException.class, () -> walletservice.login(LoginReq));
    }

    @Test
    public void loginUser_InvalidPassword_ThrowsRuntimeException() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword(new BCryptPasswordEncoder().encode("password"));

        when(userRepo.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        LoginReq LoginReq = new LoginReq();
        LoginReq.setEmail("test@test.com");
        LoginReq.setPassword("invalid_password");

        assertThrows(RuntimeException.class, () -> walletservice.login(LoginReq));
    }

    @Test
    public void testUserDataSuccess() {
        UserReq UserReq = new UserReq();
        UserReq.setToken("valid_token");

        User user = new User();
        user.setId("1234");
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setWalletBalance(100.0);
        user.setPassword("password");

        TransactionData transactionList = new TransactionData();
        transactionList.setId("5678");
        transactionList.setUserId(user.getId());
        transactionList.setTransaction(Collections.emptyList());

        when(jwtFeature.extractExpiration(UserReq.getToken())).thenReturn(new Date(System.currentTimeMillis() + 3600000));
        when(jwtFeature.extractUsername(UserReq.getToken())).thenReturn(user.getEmail());
        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(transactionListRepository.findByUserId(user.getId())).thenReturn(Collections.singletonList(transactionList));

        UserDataResponse response = walletservice.data(UserReq);
        assertNotNull(response);
        assertEquals(user.getUsername(), response.getUser().getUsername());
        assertEquals(user.getEmail(), response.getUser().getEmail());
        assertEquals(user.getWalletBalance(), response.getUser().getWalletBalance(), 0.0);
        assertNull(response.getUser().getPassword());
        assertEquals(1, response.getTransactions().size());
        assertEquals(transactionList.getId(), response.getTransactions().getFirst().getId());
        assertEquals(transactionList.getUserId(), response.getTransactions().getFirst().getUserId());
        assertEquals(transactionList.getTransaction(), response.getTransactions().getFirst().getTransaction());
    }

    @Test
    public void testUserDataTokenExpired() {
        UserReq UserReq = new UserReq();
        UserReq.setToken("expired_token");

        when(jwtFeature.extractExpiration(UserReq.getToken())).thenReturn(new Date(System.currentTimeMillis() - 3600000));

//        walletservice.data(UserReq);

        assertThrows(RuntimeException.class, () -> walletservice.data(UserReq));
    }

    @Test
    public void testUserDataUserNotFound() {
        UserReq UserReq = new UserReq();
        UserReq.setToken("valid_token");

        when(jwtFeature.extractExpiration(UserReq.getToken())).thenReturn(new Date(System.currentTimeMillis() + 3600000));
        when(jwtFeature.extractUsername(UserReq.getToken())).thenReturn("unknown_user@example.com");
        when(userRepo.findByEmail("unknown_user@example.com")).thenReturn(Optional.empty());

//        walletservice.data(UserReq);
        assertThrows(RuntimeException.class, () -> walletservice.data(UserReq));
    }


    @Test
    public void testTransferAmountUserNotFound() {
        String fromEmail = "from@example.com";
        String toEmail = "to@example.com";
        double fromBalance = 100.0;
        double transferAmount = 50.0;
        User fromUser = new User();
        fromUser.setEmail(fromEmail);
        fromUser.setPassword("password");
        fromUser.setWalletBalance(fromBalance);
        TransferReq transferReq = new TransferReq(fromEmail,toEmail,10 );
        transferReq.setAmount(transferAmount);
        String token = "Bearer " + jwtFeature.generateToken(fromEmail);
        when(jwtFeature.extractUsername(anyString())).thenReturn(fromEmail);
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.empty());

        ApiResponse response = walletservice.transfer(transferReq, token);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals("User not found", response.getResponse());
    }

    @Test
    public void testRechargeWallet_InvalidAmount() {
        String email = "yeshwanth.idiga@nextuple.com";
        RechargeReq rechargeReq = new RechargeReq();
        rechargeReq.setAmount(-100);
        rechargeReq.setEmail(email);
        when(jwtFeature.extractUsername(anyString())).thenReturn(email);
        String authHeader = "Bearer " + jwtFeature.generateToken(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(new User()));
        ApiResponse response = walletservice.recharge(rechargeReq, authHeader);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals("Invalid amount", response.getResponse());
    }

    @Test
    public void testRechargeWallet_InvalidUser() {
        RechargeReq RechargeReq = new RechargeReq();
        when(jwtFeature.extractUsername(anyString())).thenReturn("admin@gmail.com");
        RechargeReq.setAmount(100);
        String authHeader = "Bearer " + jwtFeature.generateToken("admin@gmail.com");
        when(userRepo.findByEmail("admin@gmail.com")).thenReturn(Optional.empty());

        ApiResponse response = walletservice.recharge(RechargeReq, authHeader);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals("Not authorized user", response.getResponse());
    }

    @Test
    public void testTransferWithUnauthorizedUser() {
        TransferReq TransferReq = new TransferReq();
        TransferReq.setAmount(0); // set amount to 0, which is an invalid value

        ApiResponse response = walletservice.transfer(TransferReq, "valid_auth_token");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());

        assertEquals("User not found", response.getResponse());
    }


    @Test
    public void testTransferUserNotFound() {
        TransferReq TransferReq = new TransferReq();
        TransferReq.setToEmail("admin@gmail.com");
        String username = "admin@gmail.com";

        ApiResponse response = walletservice.transfer(TransferReq, "Bearer " + jwtFeature.generateToken(username));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals("User not found", response.getResponse());
    }

    @Test
    public void testTransferAmount_Successful() throws Exception {
        // Prepare test data

        String authHeader = "Bearer token";

        // Mock service dependencies
        User fromUser = new User();
        fromUser.setId("1L");
        fromUser.setEmail("user1@test.com");
        fromUser.setWalletBalance(200.0);
        User toUser = new User();
        toUser.setId("2L");
        toUser.setEmail("user2@test.com");
        toUser.setWalletBalance(100.0);

        TransferReq transferReq = new TransferReq();
        transferReq.setToEmail(toUser.getEmail());
        transferReq.setFromEmail(fromUser.getEmail());
        transferReq.setAmount(50.0);

        when(jwtFeature.extractUsername("token")).thenReturn(fromUser.getEmail());
        when(userRepo.findByEmail(fromUser.getEmail())).thenReturn(Optional.of(fromUser));
        when(userRepo.findByEmail(toUser.getEmail())).thenReturn(Optional.of(toUser));

        // Perform the test
        ApiResponse response = walletservice.transfer(transferReq, authHeader);

        // Verify the result
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("Transfer successful", response.getResponse());

        // Verify that wallet balances are updated correctly
        assertEquals(150, fromUser.getWalletBalance(), 0.0);
        assertEquals(150, toUser.getWalletBalance(), 0.0);

        // Verify that transactions are saved
        verify(transactionListRepository, times(1)).save(any(TransactionDataImpl.class));

    }


}

