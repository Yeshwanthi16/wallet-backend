package com.example.wallet.mapper;

import com.example.wallet.entity.TransactionDataImpl;
import com.example.wallet.model.dto.TransactionData;
import org.mapstruct.Mapper;

@Mapper
public interface TransactionDataMapper {

//    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

//    TransactionData modelToDto(TransactionDataEntity transactionDataEntity);

    TransactionDataImpl dtoToModel(TransactionData transactionData);
}
