package com.example.wallet.Controller;

import com.example.wallet.model.dto.*;
import com.example.wallet.repo.TransactionDataRepo;
import com.example.wallet.repo.UserRepo;


import com.example.wallet.features.EmailFeature;
import com.example.wallet.features.JwtFeature;
import com.example.wallet.service.WalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class WalletServiceTests {

    @InjectMocks
    private WalletService walletservice;

    @Mock
    private UserRepo userRepository;

    @Mock
    private JwtFeature jwtFeature;

    @Mock
    private EmailFeature emailService;

    @Mock
    private TransactionDataRepo transactionListRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testRegisterUserSuccess() {
        User registrationRequest = new User();
        registrationRequest.setUsername("johndoe");
        registrationRequest.setEmail("johndoe@example.com");
        registrationRequest.setPassword("password");

//        when(userRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.empty());

        ApiResponse response = walletservice.createUser(registrationRequest);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("User created successfully", response.getResponse());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertNotNull(savedUser.getId());
        assertEquals(registrationRequest.getUsername(), savedUser.getUsername());
        assertNotEquals(registrationRequest.getPassword(), savedUser.getPassword());
        assertTrue(new BCryptPasswordEncoder().matches(registrationRequest.getPassword(), savedUser.getPassword()));
        assertEquals(registrationRequest.getEmail(), savedUser.getEmail());
        assertEquals(0.0, savedUser.getWalletBalance(), 0.0);

        ArgumentCaptor<TransactionData> transactionListCaptor = ArgumentCaptor.forClass(TransactionData.class);
        verify(transactionListRepository).save(transactionListCaptor.capture());
        TransactionData savedTransactionList = transactionListCaptor.getValue();
        assertNotNull(savedTransactionList.getUserId());
        assertEquals(savedUser.getId(), savedTransactionList.getUserId());
        assertTrue(savedTransactionList.getTransaction().isEmpty());

        ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(toCaptor.capture());
        assertEquals(registrationRequest.getEmail(), toCaptor.getValue());
    }

    @Test
    public void testRegisterUserAccountExists() {
        User registrationRequest = new User();
        registrationRequest.setEmail("johndoe@example.com");

        when(userRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.of(new User()));

        ApiResponse response = walletservice.createUser(registrationRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals("Account exists with this email already", response.getResponse());

        verify(userRepository, never()).save(any(User.class));
        verify(transactionListRepository, never()).save(any(TransactionData.class));
//        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void loginUser_ValidUser_ReturnsToken() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword(new BCryptPasswordEncoder().encode("password"));

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(jwtFeature.generateToken("test@test.com")).thenReturn("test_token");

        LoginReq LoginReq = new LoginReq();
        LoginReq.setEmail("test@test.com");
        LoginReq.setPassword("password");

        String result = walletservice.login(LoginReq);

        assertEquals("test_token", result);
    }

    @Test
    public void loginUser_InvalidUser_ThrowsRuntimeException() {
//        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        LoginReq LoginReq = new LoginReq();
        LoginReq.setEmail("test@test.com");
        LoginReq.setPassword("password");

        assertThrows(RuntimeException.class, () -> walletservice.login(LoginReq));
    }

    @Test
    public void loginUser_InvalidPassword_ThrowsRuntimeException() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword(new BCryptPasswordEncoder().encode("password"));

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

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
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
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

    @Test(expected = RuntimeException.class)
    public void testUserDataTokenExpired() {
        UserReq UserReq = new UserReq();
        UserReq.setToken("expired_token");

        when(jwtFeature.extractExpiration(UserReq.getToken())).thenReturn(new Date(System.currentTimeMillis() - 3600000));

        walletservice.data(UserReq);
    }

    @Test(expected = RuntimeException.class)
    public void testUserDataUserNotFound() {
        UserReq UserReq = new UserReq();
        UserReq.setToken("valid_token");

        when(jwtFeature.extractExpiration(UserReq.getToken())).thenReturn(new Date(System.currentTimeMillis() + 3600000));
        when(jwtFeature.extractUsername(UserReq.getToken())).thenReturn("unknown_user@example.com");
        when(userRepository.findByEmail("unknown_user@example.com")).thenReturn(Optional.empty());

        walletservice.data(UserReq);
    }


    @Test
    public void testTransferAmountUserNotFound() {
        String fromEmail = "from@example.com";
        double fromBalance = 100.0;
        double transferAmount = 50.0;
        User fromUser = new User();
        fromUser.setEmail(fromEmail);
        fromUser.setPassword("password");
        fromUser.setWalletBalance(fromBalance);
        User toUser = new User();
        TransferReq TransferReq = new TransferReq();
        TransferReq.setAmount(transferAmount);
        String token = "Bearer " + jwtFeature.generateToken(fromEmail);
//        when(userRepository.findByEmail(fromEmail)).thenReturn(Optional.of(fromUser));
//        when(userRepository.findByEmail("random")).thenReturn(Optional.of(toUser));

        ApiResponse response = walletservice.transfer(TransferReq, token);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals("User not found", response.getResponse());
    }

    @Test(expected = RuntimeException.class)
    public void testRechargeWallet_InvalidAmount() {
        RechargeReq RechargeReq = new RechargeReq();
        RechargeReq.setAmount(-100);
        String authHeader = "Bearer " + jwtFeature.generateToken("testuser@example.com");

        ApiResponse response = walletservice.recharge(RechargeReq, authHeader);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals("Invalid amount", response.getResponse());
    }

    @Test(expected = RuntimeException.class)
    public void testRechargeWallet_InvalidUser() {
        RechargeReq RechargeReq = new RechargeReq();
        RechargeReq.setAmount(100);
        String authHeader = "Bearer " + jwtFeature.generateToken("admin@gmail.com");

//        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.empty());

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

        assertEquals("Unauthorized", response.getResponse());
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


    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(walletservice).build();
    }

    @Test
    public void rechargeWallet_Successful() throws Exception {
        String username = "user@test.com";
        String token = "Bearer "+ jwtFeature.generateToken(username);
        User user = new User();

        user.setUsername(username);
        user.setPassword("admin");

        RechargeReq RechargeReq = new RechargeReq();
        RechargeReq.setAmount(100.0);

        when(jwtFeature.extractUsername(token.substring(7))).thenReturn(username);
        when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(post("http://localhost:8080/wallet/recharge")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(RechargeReq)))
                .andExpect(status().isOk());
    }

    @Test
    public void transferAmount_Successful() throws Exception {
        String username = "user1@test.com";
        String token = "Bearer " + jwtFeature.generateToken(username);

        User fromUser = new User();
        fromUser.setId("1L");
        fromUser.setEmail(username);
        fromUser.setWalletBalance(200.0);

        User toUser = new User();
        toUser.setId("2L");
        toUser.setEmail("user2@test.com");
        toUser.setWalletBalance(100.0);

        when(jwtFeature.extractUsername(token.substring(7))).thenReturn(username);
        when(userRepository.findByEmail(username)).thenReturn(Optional.of(fromUser));
        when(userRepository.findByEmail(toUser.getEmail())).thenReturn(Optional.of(toUser));

        TransferReq TransferReq = new TransferReq();
        TransferReq.setToEmail(toUser.getEmail());
        TransferReq.setAmount(50.0);

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(post("http://localhost:8080/wallet/transfer")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TransferReq)))
                .andExpect(status().isOk());

        assertEquals(fromUser.getWalletBalance(), 150.0, 0.0);
        assertEquals(toUser.getWalletBalance(), 150.0, 0.0);

        ArgumentCaptor<TransactionData> fromUserTransactionListCaptor = ArgumentCaptor.forClass(TransactionData.class);
        verify(transactionListRepository, times(2)).save(fromUserTransactionListCaptor.capture());
        List<TransactionData> capturedFromUserTransactionLists = fromUserTransactionListCaptor.getAllValues();
        assertEquals(capturedFromUserTransactionLists.getFirst().getUserId(), fromUser.getId());
        assertEquals(capturedFromUserTransactionLists.getFirst().getTransaction().size(), 1);

        ArgumentCaptor<TransactionData> toUserTransactionListCaptor = ArgumentCaptor.forClass(TransactionData.class);
        verify(transactionListRepository, times(2)).save(toUserTransactionListCaptor.capture());
        List<TransactionData> capturedToUserTransactionLists = toUserTransactionListCaptor.getAllValues();

    }


}
