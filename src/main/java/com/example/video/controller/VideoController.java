package com.example.video.controller;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.video.dto.CommentDto;
import com.example.video.dto.UploadVideoResponse;
import com.example.video.dto.VideoDto;
import com.example.video.service.VideoService;

import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UploadVideoResponse uploadVideo(@RequestParam("file") MultipartFile file) { 
        return videoService.uploadVideo(file); 
    }
    
    @PostMapping("/thumbnail")
    @ResponseStatus(HttpStatus.CREATED)
    public String uploadThumbnail(@RequestParam("file") MultipartFile file, @RequestParam("videoId") String videoId) { 
        return videoService.uploadThumbnail(file, videoId); 
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public VideoDto editVideoMetadata(@RequestBody VideoDto videoDto) {
        return videoService.editVideo(videoDto);
    }

    @GetMapping("/{videoId}")
    @ResponseStatus(HttpStatus.OK)
    public VideoDto getVideoDetails(@PathVariable("videoId") String videoId) {
        return videoService.getVideoDetails(videoId);
    }

    @PostMapping("/{videoId}/like")
    @ResponseStatus(HttpStatus.OK)
    public VideoDto likeVideo(@PathVariable("videoId") String videoId) {
        return videoService.likeVideo(videoId);
    }

    @PostMapping("/{videoId}/disLike")
    @ResponseStatus(HttpStatus.OK)
    public VideoDto disLikeVideo(@PathVariable("videoId") String videoId) {
        return videoService.disLikeVideo(videoId);
    }

    @PostMapping("/{videoId}/comment")
    @ResponseStatus(HttpStatus.OK)
    public void addComments(@PathVariable("videoId") String videoId, @RequestBody CommentDto commentDto) {
        videoService.addComments(videoId, commentDto);
    }

    @GetMapping("/{videoId}/comment")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getAllComments(@PathVariable("videoId") String videoId) {
        return videoService.getAllComments(videoId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<VideoDto> getAllVideos() {
        return videoService.getAllVideos();
    }
}
