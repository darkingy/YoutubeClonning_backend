package com.example.video.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.video.model.Video;

public interface VideoRepository extends MongoRepository<Video, String>{
    
}
