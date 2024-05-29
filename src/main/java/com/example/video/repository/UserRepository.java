package com.example.video.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.video.model.User;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findBySub(String sub);
}
