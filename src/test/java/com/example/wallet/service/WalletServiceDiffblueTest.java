package com.example.wallet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.wallet.features.EmailFeature;
import com.example.wallet.features.JwtFeature;
import com.example.wallet.model.Transaction;
import com.example.wallet.model.dto.ApiResponse;
import com.example.wallet.model.dto.LoginReq;
import com.example.wallet.model.dto.RechargeReq;
import com.example.wallet.model.dto.TransactionData;
import com.example.wallet.model.dto.TransferReq;
import com.example.wallet.model.dto.User;
import com.example.wallet.model.dto.UserDataResponse;
import com.example.wallet.model.dto.UserReq;
import com.example.wallet.repo.TransactionDataRepo;
import com.example.wallet.repo.UserRepo;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = {WalletService.class})
@ExtendWith(SpringExtension.class)
class WalletServiceDiffblueTest {
    @MockBean
    private EmailFeature emailFeature;

    @MockBean
    private JwtFeature jwtFeature;

    @MockBean
    private TransactionDataRepo transactionDataRepo;

    @MockBean
    private UserRepo userRepo;

    @Autowired
    private WalletService walletService;

    /**
     * Method under test: {@link WalletService#createUser(User)}
     */
    @Test
    void testCreateUser() {
        // Arrange
        User user = new User();
        user.setEmail("jane.doe@example.org");
        user.setId("42");
        user.setPassword("iloveyou");
        user.setUsername("janedoe");
        user.setWalletBalance(10.0d);
        Optional<User> ofResult = Optional.of(user);
        when(userRepo.findByEmail(Mockito.<String>any())).thenReturn(ofResult);

        User userReq = new User();
        userReq.setEmail("jane.doe@example.org");
        userReq.setId("42");
        userReq.setPassword("iloveyou");
        userReq.setUsername("janedoe");
        userReq.setWalletBalance(10.0d);

        // Act
        ApiResponse actualCreateUserResult = walletService.createUser(userReq);

        // Assert
        verify(userRepo).findByEmail(Mockito.<String>any());
        assertEquals("Account exists with this email already", actualCreateUserResult.getResponse());
        assertEquals(HttpStatus.BAD_REQUEST, actualCreateUserResult.getStatus());
    }

    /**
     * Method under test: {@link WalletService#createUser(User)}
     */
    @Test
    void testCreateUser2() {
        // Arrange
        User user = new User();
        user.setEmail("jane.doe@example.org");
        user.setId("42");
        user.setPassword("iloveyou");
        user.setUsername("janedoe");
        user.setWalletBalance(10.0d);
        when(userRepo.save(Mockito.<User>any())).thenReturn(user);
        Optional<User> emptyResult = Optional.empty();
        when(userRepo.findByEmail(Mockito.<String>any())).thenReturn(emptyResult);

        TransactionData transactionData = new TransactionData();
        transactionData.setId("42");
        ArrayList<Transaction> transaction = new ArrayList<>();
        transactionData.setTransaction(transaction);
        transactionData.setUserId("42");
        when(transactionDataRepo.save(Mockito.<TransactionData>any())).thenReturn(transactionData);
        doNothing().when(emailFeature).sendEmail(Mockito.<String>any());

        User userReq = new User();
        userReq.setEmail("jane.doe@example.org");
        userReq.setId("42");
        userReq.setPassword("iloveyou");
        userReq.setUsername("janedoe");
        userReq.setWalletBalance(10.0d);

        // Act
        ApiResponse actualCreateUserResult = walletService.createUser(userReq);

        // Assert
        verify(emailFeature).sendEmail(Mockito.<String>any());
        verify(userRepo).findByEmail(Mockito.<String>any());
        verify(transactionDataRepo).save(Mockito.<TransactionData>any());
        verify(userRepo).save(Mockito.<User>any());
        assertEquals("User created successfully", actualCreateUserResult.getResponse());
        User user2 = walletService.user;
        assertEquals("jane.doe@example.org", user2.getEmail());
        assertEquals("janedoe", user2.getUsername());
        assertEquals(10.0d, user2.getWalletBalance().doubleValue());
        assertEquals(HttpStatus.OK, actualCreateUserResult.getStatus());
        assertEquals(transaction, walletService.transactionData.getTransaction());
    }

    /**
     * Method under test: {@link WalletService#createUser(User)}
     */
    @Test
    void testCreateUser3() {
        // Arrange
        User user = new User();
        user.setEmail("jane.doe@example.org");
        user.setId("42");
        user.setPassword("iloveyou");
        user.setUsername("janedoe");
        user.setWalletBalance(10.0d);
        when(userRepo.save(Mockito.<User>any())).thenReturn(user);
        Optional<User> emptyResult = Optional.empty();
        when(userRepo.findByEmail(Mockito.<String>any())).thenReturn(emptyResult);

        TransactionData transactionData = new TransactionData();
        transactionData.setId("42");
        transactionData.setTransaction(new ArrayList<>());
        transactionData.setUserId("42");
        when(transactionDataRepo.save(Mockito.<TransactionData>any())).thenReturn(transactionData);
        doThrow(new RuntimeException("User created successfully")).when(emailFeature).sendEmail(Mockito.<String>any());

        User userReq = new User();
        userReq.setEmail("jane.doe@example.org");
        userReq.setId("42");
        userReq.setPassword("iloveyou");
        userReq.setUsername("janedoe");
        userReq.setWalletBalance(10.0d);

        // Act and Assert
        assertThrows(RuntimeException.class, () -> walletService.createUser(userReq));
        verify(emailFeature).sendEmail(Mockito.<String>any());
        verify(userRepo).findByEmail(Mockito.<String>any());
        verify(transactionDataRepo).save(Mockito.<TransactionData>any());
        verify(userRepo).save(Mockito.<User>any());
    }

    /**
     * Method under test: {@link WalletService#createUser(User)}
     */
    @Test
    void testCreateUser4() {
        // Arrange
        Optional<User> emptyResult = Optional.empty();
        when(userRepo.findByEmail(Mockito.<String>any())).thenReturn(emptyResult);
        User userReq = mock(User.class);
        when(userReq.getUsername()).thenThrow(new RuntimeException("foo"));
        when(userReq.getEmail()).thenReturn("jane.doe@example.org");
        when(userReq.getPassword()).thenReturn("iloveyou");
        doNothing().when(userReq).setEmail(Mockito.<String>any());
        doNothing().when(userReq).setId(Mockito.<String>any());
        doNothing().when(userReq).setPassword(Mockito.<String>any());
        doNothing().when(userReq).setUsername(Mockito.<String>any());
        doNothing().when(userReq).setWalletBalance(Mockito.<Double>any());
        userReq.setEmail("jane.doe@example.org");
        userReq.setId("42");
        userReq.setPassword("iloveyou");
        userReq.setUsername("janedoe");
        userReq.setWalletBalance(10.0d);

        // Act and Assert
        assertThrows(RuntimeException.class, () -> walletService.createUser(userReq));
        verify(userReq).getEmail();
        verify(userReq).getPassword();
        verify(userReq).getUsername();
        verify(userReq).setEmail(Mockito.<String>any());
        verify(userReq).setId(Mockito.<String>any());
        verify(userReq).setPassword(Mockito.<String>any());
        verify(userReq).setUsername(Mockito.<String>any());
        verify(userReq).setWalletBalance(Mockito.<Double>any());
        verify(userRepo).findByEmail(Mockito.<String>any());
    }

    /**
     * Method under test: {@link WalletService#login(LoginReq)}
     */
    @Test
    void testLogin() {
        // Arrange
        User user = new User();
        user.setEmail("jane.doe@example.org");
        user.setId("42");
        user.setPassword("iloveyou");
        user.setUsername("janedoe");
        user.setWalletBalance(10.0d);
        Optional<User> ofResult = Optional.of(user);
        when(userRepo.findByEmail(Mockito.<String>any())).thenReturn(ofResult);

        // Act and Assert
        assertThrows(RuntimeException.class, () -> walletService.login(new LoginReq("jane.doe@example.org", "iloveyou")));
        verify(userRepo).findByEmail(Mockito.<String>any());
    }

    /**
     * Method under test: {@link WalletService#login(LoginReq)}
     */
    @Test
    void testLogin2() {
        // Arrange
        Optional<User> emptyResult = Optional.empty();
        when(userRepo.findByEmail(Mockito.<String>any())).thenReturn(emptyResult);

        // Act and Assert
        assertThrows(RuntimeException.class, () -> walletService.login(new LoginReq("jane.doe@example.org", "iloveyou")));
        verify(userRepo).findByEmail(Mockito.<String>any());
    }

    /**
     * Method under test: {@link WalletService#login(LoginReq)}
     */
    @Test
    void testLogin3() {
        // Arrange
        when(userRepo.findByEmail(Mockito.<String>any())).thenThrow(new RuntimeException("Invalid email or password"));

        // Act and Assert
        assertThrows(RuntimeException.class, () -> walletService.login(new LoginReq("jane.doe@example.org", "iloveyou")));
        verify(userRepo).findByEmail(Mockito.<String>any());
    }

    /**
     * Method under test: {@link WalletService#data(UserReq)}
     */
    @Test
    void testData() {
        // Arrange
        when(jwtFeature.extractExpiration(Mockito.<String>any()))
                .thenReturn(Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant()));

        // Act and Assert
        assertThrows(RuntimeException.class, () -> walletService.data(new UserReq("ABC123")));
        verify(jwtFeature).extractExpiration(Mockito.<String>any());
    }

    /**
     * Method under test: {@link WalletService#data(UserReq)}
     */
    @Test
    void testData2() {
        // Arrange
        User user = new User();
        user.setEmail("jane.doe@example.org");
        user.setId("42");
        user.setPassword("iloveyou");
        user.setUsername("janedoe");
        user.setWalletBalance(10.0d);
        Optional<User> ofResult = Optional.of(user);
        when(userRepo.findByEmail(Mockito.<String>any())).thenReturn(ofResult);
        when(transactionDataRepo.findByUserId(Mockito.<String>any())).thenReturn(new ArrayList<>());
        java.sql.Date date = mock(java.sql.Date.class);
        when(date.before(Mockito.<java.util.Date>any())).thenReturn(false);
        when(jwtFeature.extractUsername(Mockito.<String>any())).thenReturn("janedoe");
        when(jwtFeature.extractExpiration(Mockito.<String>any())).thenReturn(date);

        // Act
        UserDataResponse actualDataResult = walletService.data(new UserReq("ABC123"));

        // Assert
        verify(jwtFeature).extractExpiration(Mockito.<String>any());
        verify(jwtFeature).extractUsername(Mockito.<String>any());
        verify(transactionDataRepo).findByUserId(Mockito.<String>any());
        verify(userRepo).findByEmail(Mockito.<String>any());
        verify(date).before(Mockito.<java.util.Date>any());
        User user2 = actualDataResult.getUser();
        assertNull(user2.getPassword());
        assertTrue(actualDataResult.getTransactions().isEmpty());
        assertSame(user2, walletService.user);
        assertSame(walletService.user, user2);
    }

    /**
     * Method under test: {@link WalletService#data(UserReq)}
     */
    @Test
    void testData3() {
        // Arrange
        Optional<User> emptyResult = Optional.empty();
        when(userRepo.findByEmail(Mockito.<String>any())).thenReturn(emptyResult);
        java.sql.Date date = mock(java.sql.Date.class);
        when(date.before(Mockito.<java.util.Date>any())).thenReturn(false);
        when(jwtFeature.extractUsername(Mockito.<String>any())).thenReturn("janedoe");
        when(jwtFeature.extractExpiration(Mockito.<String>any())).thenReturn(date);

        // Act and Assert
        assertThrows(RuntimeException.class, () -> walletService.data(new UserReq("ABC123")));
        verify(jwtFeature).extractExpiration(Mockito.<String>any());
        verify(jwtFeature).extractUsername(Mockito.<String>any());
        verify(userRepo).findByEmail(Mockito.<String>any());
        verify(date).before(Mockito.<java.util.Date>any());
    }

    /**
     * Method under test: {@link WalletService#recharge(RechargeReq, String)}
     */
    @Test
    void testRecharge() {
        // Arrange and Act
        ApiResponse actualRechargeResult = walletService.recharge(new RechargeReq("jane.doe@example.org", 10.0d),
                "Auth Header");

        // Assert
        assertEquals("Unauthorized", actualRechargeResult.getResponse());
        assertEquals(HttpStatus.BAD_REQUEST, actualRechargeResult.getStatus());
    }

    /**
     * Method under test: {@link WalletService#recharge(RechargeReq, String)}
     */
    @Test
    void testRecharge2() {
        // Arrange and Act
        ApiResponse actualRechargeResult = walletService.recharge(mock(RechargeReq.class), "Auth Header");

        // Assert
        assertEquals("Unauthorized", actualRechargeResult.getResponse());
        assertEquals(HttpStatus.BAD_REQUEST, actualRechargeResult.getStatus());
    }

    /**
     * Method under test: {@link WalletService#recharge(RechargeReq, String)}
     */
    @Test
    void testRecharge3() {
        // Arrange
        User user = new User();
        user.setEmail("jane.doe@example.org");
        user.setId("42");
        user.setPassword("iloveyou");
        user.setUsername("janedoe");
        user.setWalletBalance(10.0d);
        Optional<User> ofResult = Optional.of(user);

        User user2 = new User();
        user2.setEmail("jane.doe@example.org");
        user2.setId("42");
        user2.setPassword("iloveyou");
        user2.setUsername("janedoe");
        user2.setWalletBalance(10.0d);
        when(userRepo.save(Mockito.<User>any())).thenReturn(user2);
        when(userRepo.findByEmail(Mockito.<String>any())).thenReturn(ofResult);

        TransactionData transactionData = new TransactionData();
        transactionData.setId("42");
        transactionData.setTransaction(new ArrayList<>());
        transactionData.setUserId("42");
        when(transactionDataRepo.save(Mockito.<TransactionData>any())).thenReturn(transactionData);
        when(transactionDataRepo.findByUserId(Mockito.<String>any())).thenReturn(new ArrayList<>());
        when(jwtFeature.extractUsername(Mockito.<String>any())).thenReturn("janedoe");

        // Act
        ApiResponse actualRechargeResult = walletService.recharge(new RechargeReq("jane.doe@example.org", 10.0d),
                "Bearer ");

        // Assert
        verify(jwtFeature).extractUsername(Mockito.<String>any());
        verify(transactionDataRepo).findByUserId(Mockito.<String>any());
        verify(userRepo).findByEmail(Mockito.<String>any());
        verify(transactionDataRepo).save(Mockito.<TransactionData>any());
        verify(userRepo).save(Mockito.<User>any());
        TransactionData transactionData2 = walletService.transactionData;
        assertEquals("42", transactionData2.getUserId());
        List<Transaction> transaction = transactionData2.getTransaction();
        assertEquals(2, transaction.size());
        Transaction getResult = transaction.get(1);
        assertEquals("Cashback", getResult.getType());
        Transaction getResult2 = transaction.get(0);
        assertEquals("Recharge", getResult2.getType());
        assertEquals("Wallet recharged successfully with cashback", actualRechargeResult.getResponse());
        assertEquals("jane.doe@example.org", getResult2.getEmail());
        assertEquals("jane.doe@example.org", getResult.getEmail());
        assertEquals(0.1d, getResult.getAmount().doubleValue());
        assertEquals(10.0d, getResult2.getAmount().doubleValue());
        assertEquals(20.1d, walletService.user.getWalletBalance().doubleValue());
        assertEquals(HttpStatus.OK, actualRechargeResult.getStatus());
    }

    /**
     * Method under test: {@link WalletService#recharge(RechargeReq, String)}
     */
    @Test
    void testRecharge4() {
        // Arrange
        when(jwtFeature.extractUsername(Mockito.<String>any())).thenThrow(new RuntimeException("Bearer "));

        // Act and Assert
        assertThrows(RuntimeException.class,
                () -> walletService.recharge(new RechargeReq("jane.doe@example.org", 10.0d), "Bearer "));
        verify(jwtFeature).extractUsername(Mockito.<String>any());
    }

    /**
     * Method under test: {@link WalletService#recharge(RechargeReq, String)}
     */
    @Test
    void testRecharge5() {
        // Arrange
        Optional<User> emptyResult = Optional.empty();
        when(userRepo.findByEmail(Mockito.<String>any())).thenReturn(emptyResult);
        when(jwtFeature.extractUsername(Mockito.<String>any())).thenReturn("janedoe");

        // Act
        ApiResponse actualRechargeResult = walletService.recharge(new RechargeReq("jane.doe@example.org", 10.0d),
                "Bearer ");

        // Assert
        verify(jwtFeature).extractUsername(Mockito.<String>any());
        verify(userRepo).findByEmail(Mockito.<String>any());
        assertEquals("Not authorized user", actualRechargeResult.getResponse());
        assertEquals(HttpStatus.BAD_REQUEST, actualRechargeResult.getStatus());
    }

    /**
     * Method under test: {@link WalletService#recharge(RechargeReq, String)}
     */
    @Test
    void testRecharge6() {
        // Arrange
        User user = new User();
        user.setEmail("jane.doe@example.org");
        user.setId("42");
        user.setPassword("iloveyou");
        user.setUsername("janedoe");
        user.setWalletBalance(10.0d);
        Optional<User> ofResult = Optional.of(user);

        User user2 = new User();
        user2.setEmail("jane.doe@example.org");
        user2.setId("42");
        user2.setPassword("iloveyou");
        user2.setUsername("janedoe");
        user2.setWalletBalance(10.0d);
        when(userRepo.save(Mockito.<User>any())).thenReturn(user2);
        when(userRepo.findByEmail(Mockito.<String>any())).thenReturn(ofResult);

        TransactionData transactionData = new TransactionData();
        transactionData.setId("42");
        transactionData.setTransaction(new ArrayList<>());
        transactionData.setUserId("42");

        TransactionData transactionData2 = new TransactionData();
        transactionData2.setId("42");
        transactionData2.setTransaction(new ArrayList<>());
        transactionData2.setUserId("42");

        ArrayList<TransactionData> transactionDataList = new ArrayList<>();
        transactionDataList.add(transactionData2);
        when(transactionDataRepo.save(Mockito.<TransactionData>any())).thenReturn(transactionData);
        when(transactionDataRepo.findByUserId(Mockito.<String>any())).thenReturn(transactionDataList);
        when(jwtFeature.extractUsername(Mockito.<String>any())).thenReturn("janedoe");

        // Act
        ApiResponse actualRechargeResult = walletService.recharge(new RechargeReq("jane.doe@example.org", 10.0d),
                "Bearer ");

        // Assert
        verify(jwtFeature).extractUsername(Mockito.<String>any());
        verify(transactionDataRepo).findByUserId(Mockito.<String>any());
        verify(userRepo).findByEmail(Mockito.<String>any());
        verify(transactionDataRepo).save(Mockito.<TransactionData>any());
        verify(userRepo).save(Mockito.<User>any());
        assertEquals("Wallet recharged successfully with cashback", actualRechargeResult.getResponse());
        assertEquals(20.1d, walletService.user.getWalletBalance().doubleValue());
        assertEquals(HttpStatus.OK, actualRechargeResult.getStatus());
    }

    /**
     * Method under test: {@link WalletService#recharge(RechargeReq, String)}
     */
    @Test
    void testRecharge7() {
        // Arrange
        User user = new User();
        user.setEmail("jane.doe@example.org");
        user.setId("42");
        user.setPassword("iloveyou");
        user.setUsername("janedoe");
        user.setWalletBalance(10.0d);
        Optional<User> ofResult = Optional.of(user);
        when(userRepo.findByEmail(Mockito.<String>any())).thenReturn(ofResult);
        when(jwtFeature.extractUsername(Mockito.<String>any())).thenReturn("janedoe");

        // Act
        ApiResponse actualRechargeResult = walletService.recharge(new RechargeReq("jane.doe@example.org", 0.0d), "Bearer ");

        // Assert
        verify(jwtFeature).extractUsername(Mockito.<String>any());
        verify(userRepo).findByEmail(Mockito.<String>any());
        assertEquals("Invalid amount", actualRechargeResult.getResponse());
        assertEquals(HttpStatus.BAD_REQUEST, actualRechargeResult.getStatus());
    }

    /**
     * Method under test: {@link WalletService#transfer(TransferReq, String)}
     */
    @Test
    void testTransfer() {
        // Arrange and Act
        ApiResponse actualTransferResult = walletService
                .transfer(new TransferReq("jane.doe@example.org", "jane.doe@example.org", 10.0d), "Auth Header");

        // Assert
        assertEquals("Unauthorized", actualTransferResult.getResponse());
        assertEquals(HttpStatus.BAD_REQUEST, actualTransferResult.getStatus());
    }

    /**
     * Method under test: {@link WalletService#transfer(TransferReq, String)}
     */
    @Test
    void testTransfer2() {
        // Arrange and Act
        ApiResponse actualTransferResult = walletService.transfer(mock(TransferReq.class), "Auth Header");

        // Assert
        assertEquals("Unauthorized", actualTransferResult.getResponse());
        assertEquals(HttpStatus.BAD_REQUEST, actualTransferResult.getStatus());
    }

    /**
     * Method under test: {@link WalletService#transfer(TransferReq, String)}
     */
    @Test
    void testTransfer3() {
        // Arrange
        User user = new User();
        user.setEmail("jane.doe@example.org");
        user.setId("42");
        user.setPassword("iloveyou");
        user.setUsername("janedoe");
        user.setWalletBalance(10.0d);
        Optional<User> ofResult = Optional.of(user);

        User user2 = new User();
        user2.setEmail("jane.doe@example.org");
        user2.setId("42");
        user2.setPassword("iloveyou");
        user2.setUsername("janedoe");
        user2.setWalletBalance(10.0d);
        when(userRepo.save(Mockito.<User>any())).thenReturn(user2);
        when(userRepo.findByEmail(Mockito.<String>any())).thenReturn(ofResult);

        TransactionData transactionData = new TransactionData();
        transactionData.setId("42");
        transactionData.setTransaction(new ArrayList<>());
        transactionData.setUserId("42");
        when(transactionDataRepo.save(Mockito.<TransactionData>any())).thenReturn(transactionData);
        when(transactionDataRepo.findByUserId(Mockito.<String>any())).thenReturn(new ArrayList<>());
        when(jwtFeature.extractUsername(Mockito.<String>any())).thenReturn("janedoe");

        // Act
        ApiResponse actualTransferResult = walletService
                .transfer(new TransferReq("jane.doe@example.org", "jane.doe@example.org", 10.0d), "Bearer ");

        // Assert
        verify(jwtFeature).extractUsername(Mockito.<String>any());
        verify(transactionDataRepo, atLeast(1)).findByUserId(Mockito.<String>any());
        verify(userRepo, atLeast(1)).findByEmail(Mockito.<String>any());
        verify(transactionDataRepo, atLeast(1)).save(Mockito.<TransactionData>any());
        verify(userRepo, atLeast(1)).save(Mockito.<User>any());
        assertEquals("Transfer successful", actualTransferResult.getResponse());
        assertEquals(HttpStatus.OK, actualTransferResult.getStatus());
    }

    /**
     * Method under test: {@link WalletService#transfer(TransferReq, String)}
     */
    @Test
    void testTransfer4() {
        // Arrange
        User user = new User();
        user.setEmail("jane.doe@example.org");
        user.setId("42");
        user.setPassword("iloveyou");
        user.setUsername("janedoe");
        user.setWalletBalance(10.0d);
        Optional<User> ofResult = Optional.of(user);
        when(userRepo.findByEmail(Mockito.<String>any())).thenReturn(ofResult);
        when(transactionDataRepo.findByUserId(Mockito.<String>any())).thenThrow(new RuntimeException("Bearer "));
        when(jwtFeature.extractUsername(Mockito.<String>any())).thenReturn("janedoe");

        // Act and Assert
        assertThrows(RuntimeException.class, () -> walletService
                .transfer(new TransferReq("jane.doe@example.org", "jane.doe@example.org", 10.0d), "Bearer "));
        verify(jwtFeature).extractUsername(Mockito.<String>any());
        verify(transactionDataRepo).findByUserId(Mockito.<String>any());
        verify(userRepo, atLeast(1)).findByEmail(Mockito.<String>any());
    }

    /**
     * Method under test: {@link WalletService#transfer(TransferReq, String)}
     */
    @Test
    void testTransfer5() {
        // Arrange
        User user = mock(User.class);
        when(user.getEmail()).thenThrow(new RuntimeException("foo"));
        when(user.getWalletBalance()).thenReturn(10.0d);
        doNothing().when(user).setEmail(Mockito.<String>any());
        doNothing().when(user).setId(Mockito.<String>any());
        doNothing().when(user).setPassword(Mockito.<String>any());
        doNothing().when(user).setUsername(Mockito.<String>any());
        doNothing().when(user).setWalletBalance(Mockito.<Double>any());
        user.setEmail("jane.doe@example.org");
        user.setId("42");
        user.setPassword("iloveyou");
        user.setUsername("janedoe");
        user.setWalletBalance(10.0d);
        Optional<User> ofResult = Optional.of(user);
        when(userRepo.findByEmail(Mockito.<String>any())).thenReturn(ofResult);
        when(jwtFeature.extractUsername(Mockito.<String>any())).thenReturn("janedoe");

        // Act and Assert
        assertThrows(RuntimeException.class, () -> walletService
                .transfer(new TransferReq("jane.doe@example.org", "jane.doe@example.org", 10.0d), "Bearer "));
        verify(jwtFeature).extractUsername(Mockito.<String>any());
        verify(user).getEmail();
        verify(user).getWalletBalance();
        verify(user).setEmail(Mockito.<String>any());
        verify(user).setId(Mockito.<String>any());
        verify(user).setPassword(Mockito.<String>any());
        verify(user).setUsername(Mockito.<String>any());
        verify(user).setWalletBalance(Mockito.<Double>any());
        verify(userRepo, atLeast(1)).findByEmail(Mockito.<String>any());
    }

    /**
     * Method under test: {@link WalletService#transfer(TransferReq, String)}
     */
    @Test
    void testTransfer6() {
        // Arrange
        User user = mock(User.class);
        when(user.getWalletBalance()).thenReturn(0.0d);
        doNothing().when(user).setEmail(Mockito.<String>any());
        doNothing().when(user).setId(Mockito.<String>any());
        doNothing().when(user).setPassword(Mockito.<String>any());
        doNothing().when(user).setUsername(Mockito.<String>any());
        doNothing().when(user).setWalletBalance(Mockito.<Double>any());
        user.setEmail("jane.doe@example.org");
        user.setId("42");
        user.setPassword("iloveyou");
        user.setUsername("janedoe");
        user.setWalletBalance(10.0d);
        Optional<User> ofResult = Optional.of(user);
        when(userRepo.findByEmail(Mockito.<String>any())).thenReturn(ofResult);
        when(jwtFeature.extractUsername(Mockito.<String>any())).thenReturn("janedoe");

        // Act
        ApiResponse actualTransferResult = walletService
                .transfer(new TransferReq("jane.doe@example.org", "jane.doe@example.org", 10.0d), "Bearer ");

        // Assert
        verify(jwtFeature).extractUsername(Mockito.<String>any());
        verify(user).getWalletBalance();
        verify(user).setEmail(Mockito.<String>any());
        verify(user).setId(Mockito.<String>any());
        verify(user).setPassword(Mockito.<String>any());
        verify(user).setUsername(Mockito.<String>any());
        verify(user).setWalletBalance(Mockito.<Double>any());
        verify(userRepo, atLeast(1)).findByEmail(Mockito.<String>any());
        assertEquals("Insufficient balance", actualTransferResult.getResponse());
        assertEquals(HttpStatus.BAD_REQUEST, actualTransferResult.getStatus());
    }

    /**
     * Method under test: {@link WalletService#transfer(TransferReq, String)}
     */
    @Test
    void testTransfer7() {
        // Arrange
        Optional<User> emptyResult = Optional.empty();
        when(userRepo.findByEmail(Mockito.<String>any())).thenReturn(emptyResult);
        new RuntimeException("foo");
        new RuntimeException("foo");
        when(jwtFeature.extractUsername(Mockito.<String>any())).thenReturn("janedoe");

        // Act
        ApiResponse actualTransferResult = walletService
                .transfer(new TransferReq("jane.doe@example.org", "jane.doe@example.org", 10.0d), "Bearer ");

        // Assert
        verify(jwtFeature).extractUsername(Mockito.<String>any());
        verify(userRepo, atLeast(1)).findByEmail(Mockito.<String>any());
        assertEquals("User not found", actualTransferResult.getResponse());
        assertEquals(HttpStatus.BAD_REQUEST, actualTransferResult.getStatus());
    }

    /**
     * Method under test: {@link WalletService#transfer(TransferReq, String)}
     */
    @Test
    void testTransfer8() {
        // Arrange
        User user = mock(User.class);
        doNothing().when(user).setEmail(Mockito.<String>any());
        doNothing().when(user).setId(Mockito.<String>any());
        doNothing().when(user).setPassword(Mockito.<String>any());
        doNothing().when(user).setUsername(Mockito.<String>any());
        doNothing().when(user).setWalletBalance(Mockito.<Double>any());
        user.setEmail("jane.doe@example.org");
        user.setId("42");
        user.setPassword("iloveyou");
        user.setUsername("janedoe");
        user.setWalletBalance(10.0d);
        Optional<User> ofResult = Optional.of(user);
        when(userRepo.findByEmail(Mockito.<String>any())).thenReturn(ofResult);
        when(jwtFeature.extractUsername(Mockito.<String>any())).thenReturn("jane.doe@example.org");

        // Act
        ApiResponse actualTransferResult = walletService
                .transfer(new TransferReq("jane.doe@example.org", "jane.doe@example.org", 10.0d), "Bearer ");

        // Assert
        verify(jwtFeature).extractUsername(Mockito.<String>any());
        verify(user).setEmail(Mockito.<String>any());
        verify(user).setId(Mockito.<String>any());
        verify(user).setPassword(Mockito.<String>any());
        verify(user).setUsername(Mockito.<String>any());
        verify(user).setWalletBalance(Mockito.<Double>any());
        verify(userRepo, atLeast(1)).findByEmail(Mockito.<String>any());
        assertEquals("Cannot transfer funds to oneself", actualTransferResult.getResponse());
        assertEquals(HttpStatus.BAD_REQUEST, actualTransferResult.getStatus());
    }
}
