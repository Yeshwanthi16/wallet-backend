package com.example.wallet.entity;

import com.example.wallet.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "TransactionData")
public class TransactionDataImpl implements TransactionDataEntity{
    @Id
    private String id;

    private String userId;

    private List<Transaction> transaction;

}
