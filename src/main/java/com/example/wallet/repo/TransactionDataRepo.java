package com.example.wallet.repo;

import com.example.wallet.entity.TransactionDataImpl;
import com.example.wallet.model.dto.TransactionData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TransactionDataRepo extends MongoRepository<TransactionDataImpl, String> {
    List<TransactionData> findByUserId(String userId);
}
