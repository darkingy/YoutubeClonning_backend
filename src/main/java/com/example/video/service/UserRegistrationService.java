package com.example.video.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.video.dto.UserInfoDTO;
import com.example.video.model.User;
import com.example.video.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {
    
    @Value("${auth0.userinfoEndpoint}")
    private String userInfoEndpoint;

    private final UserRepository userRepository;

    public String registerUser(String tokenValue) {
        //Make a call to the userInfo Endpoint

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(userInfoEndpoint))
                .setHeader("Authorization", String.format("Bearer %s", tokenValue))
                .build();

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();

        try {
            HttpResponse<String> responseString = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String body = responseString.body();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            UserInfoDTO userInfoDTO = objectMapper.readValue(body, UserInfoDTO.class);

            Optional<User> userBySubject = userRepository.findBySub(userInfoDTO.getSub());
            if(userBySubject.isPresent()) {
                return userBySubject.get().getId();
            } else {
                User user = new User();
                user.setFirstName(userInfoDTO.getGivenName());
                user.setLastName(userInfoDTO.getFamilyName());
                user.setFullName(userInfoDTO.getName());
                user.setEmailAddress(userInfoDTO.getEmail());
                user.setSub(userInfoDTO.getSub());
    
                return userRepository.save(user).getId();
            }

        } catch (Exception exception) {
            throw new RuntimeException("Exception occurred while registering user", exception);
        }
        //Fetch user details and save them to the database
    }
}
