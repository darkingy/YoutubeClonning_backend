package com.example.video.service;

import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import io.awspring.cloud.s3.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Service
@RequiredArgsConstructor
public class S3Service implements FileService {
    
    public static final String BUCKET_NAME = "darkingy";
    private final S3Client awsS3Client;

    @Override
    public String upload(MultipartFile file) {
        var filenameExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());

        var key = UUID.randomUUID().toString() + "." + filenameExtension;

        var metadata = ObjectMetadata.builder()
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

        try(InputStream inputStream = file.getInputStream()) {
            awsS3Client.putObject(PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .contentType(file.getContentType())
                    .build(), RequestBody.fromInputStream(inputStream, file.getSize()));
        } catch (IOException ioException) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An Exception occured while uploading the file", ioException);
        }

        awsS3Client.putObjectAcl(PutObjectAclRequest.builder()
                .bucket(BUCKET_NAME)
                .key(key)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build());

        return awsS3Client.utilities().getUrl(GetUrlRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .build()).toExternalForm();
    }
}
