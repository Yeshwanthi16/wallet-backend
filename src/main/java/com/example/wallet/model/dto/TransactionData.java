package com.example.wallet.model.dto;

import com.example.wallet.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
@NoArgsConstructor
@Data
@AllArgsConstructor
@Document(collection = "TransactionData")
@Builder
public class TransactionData {
    @Id
    private String id;

    private String userId;

    private List<Transaction> transaction;

}
