package com.example.wallet.mapper;


import com.example.wallet.entity.UserInfoEntity;
import com.example.wallet.entity.UserInfoEntityImpl;
import com.example.wallet.model.dto.User;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

//@Mapper(componentModel = "spring")
@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

//    User modelToDto(UserInfoEntity userInfoEntity);

    UserInfoEntityImpl dtoToModel(User user);

}
