package com.example.wallet.repo;

import com.example.wallet.entity.UserInfoEntityImpl;
import com.example.wallet.model.dto.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepo extends MongoRepository<UserInfoEntityImpl, String> {
    Optional<User> findByEmail(String username);
}
