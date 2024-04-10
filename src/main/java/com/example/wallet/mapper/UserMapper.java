package com.example.wallet.mapper;


import com.example.wallet.entity.UserInfoEntityImpl;
import com.example.wallet.model.dto.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

//@Mapper(componentModel = "spring")
@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

//    User modelToDto(UserInfoEntity userInfoEntity);

    UserInfoEntityImpl dtoToModel(User user);

}
