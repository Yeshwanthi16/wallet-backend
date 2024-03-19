package com.example.wallet.mapper;

import com.example.wallet.entity.TransactionDataEntity;
import com.example.wallet.entity.TransactionDataImpl;
import com.example.wallet.entity.UserInfoEntity;
import com.example.wallet.entity.UserInfoEntityImpl;
import com.example.wallet.model.dto.TransactionData;
import com.example.wallet.model.dto.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TransactionDataMapper {

//    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

//    TransactionData modelToDto(TransactionDataEntity transactionDataEntity);

    TransactionDataImpl dtoToModel(TransactionData transactionData);
}
