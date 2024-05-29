package com.example.video.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import org.springframework.web.multipart.MultipartFile;

import com.example.video.dto.CommentDto;
import com.example.video.dto.UploadVideoResponse;
import com.example.video.dto.VideoDto;
import com.example.video.model.Comment;
import com.example.video.model.Video;
import com.example.video.repository.VideoRepository;

@Service
@RequiredArgsConstructor
public class VideoService {
    
    private final S3Service s3Service;
    private final VideoRepository videoRepository;
    private final UserService userService;

    public UploadVideoResponse uploadVideo(MultipartFile multipartFile) {
        String url = s3Service.upload(multipartFile);
        var video = new Video();
        video.setUrl(url);

        var savedVideo = videoRepository.save(video);
        return new UploadVideoResponse(savedVideo.getId(), savedVideo.getUrl());
    }

    public VideoDto editVideo(VideoDto videoDto) {
        //Find the video by videoId
        var savedVideo = getVideoById(videoDto.getId());
        //Map the videoDto fields to video
        savedVideo.setTitle(videoDto.getTitle());
        savedVideo.setDescription(videoDto.getDescription());
        savedVideo.setTags(videoDto.getTags());
        savedVideo.setThumbnailUrl(videoDto.getThumbnailUrl());
        savedVideo.setVideoStatus(videoDto.getVideoStatus());

        //save the video to the database
        videoRepository.save(savedVideo);
        return videoDto;
    }

    public String uploadThumbnail(MultipartFile file, String videoId) {
        var savedVideo = getVideoById(videoId);

        String thumbnailUrl = s3Service.upload(file);

        savedVideo.setThumbnailUrl(thumbnailUrl);

        videoRepository.save(savedVideo);

        return thumbnailUrl;
    }

    Video getVideoById(String videoId) {
        return videoRepository.findById(videoId)
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find video by id: " + videoId));
    }

    public VideoDto getVideoDetails(String videoId) {
        Video savedVideo = getVideoById(videoId);

        increaseVideoCount(savedVideo);
        userService.addVideoToHistory(videoId);

        return mapToVideoDto(savedVideo);
    }

    public VideoDto likeVideo(String videoId) {
        //Get video by id
        Video videoById = getVideoById(videoId);

        //Increase like count
        //If user already liked the video, then decrement like count

        //If user already disliked the video, then increase like count and decrease dislike count

        if(userService.ifLikedVideo(videoId)) {
            videoById.decreaseLikeCount();
            userService.removeFromLikedVideos(videoId);
        } else if (userService.ifDisLikedVideo(videoId)) {
            videoById.decreaseDisLikeCount();
            userService.removeFromDisLikedVideo(videoId);
            videoById.increaseLikeCount();
            userService.addToLikedVideos(videoId);
        } else {
            videoById.increaseLikeCount();
            userService.addToLikedVideos(videoId);
        }

        videoRepository.save(videoById);

        return mapToVideoDto(videoById);
    }

    public VideoDto disLikeVideo(String videoId) {
        Video videoById = getVideoById(videoId);

        if (userService.ifDisLikedVideo(videoId)) {
            videoById.decreaseDisLikeCount();
            userService.removeFromDisLikedVideo(videoId);
        } else if (userService.ifLikedVideo(videoId)) {
            videoById.decreaseLikeCount();
            userService.removeFromLikedVideos(videoId);
            videoById.increaseDisLikeCount();
            userService.addToDisLikedVideos(videoId);
        } else {
            videoById.increaseDisLikeCount();
            userService.addToDisLikedVideos(videoId);
        }

        videoRepository.save(videoById);

        return mapToVideoDto(videoById);
    }
    
    private void increaseVideoCount(Video savedVideo) {
        savedVideo.increaseViewCount();
        videoRepository.save(savedVideo);
    }

    private VideoDto mapToVideoDto(Video videoById) {
        VideoDto videoDto = new VideoDto();
        videoDto.setUrl(videoById.getUrl());
        videoDto.setThumbnailUrl(videoById.getThumbnailUrl());
        videoDto.setId(videoById.getId());
        videoDto.setTitle(videoById.getTitle());
        videoDto.setDescription(videoById.getDescription());
        videoDto.setTags(videoById.getTags());
        videoDto.setVideoStatus(videoById.getVideoStatus());
        videoDto.setLikeCount(videoById.getLikes().get());
        videoDto.setDislikeCount(videoById.getDisLikes().get());
        videoDto.setViewCount(videoById.getViewCount().get());
        return videoDto;
    }

    public void addComments(String videoId, CommentDto commentDto) {
        Video video = getVideoById(videoId);
        Comment comment = new Comment();
        comment.setText(commentDto.getCommentText());
        comment.setAuthor(commentDto.getAuthor());
        video.addComment(comment);

        videoRepository.save(video);
    }

    public List<CommentDto> getAllComments(String videoId) {
        Video video = getVideoById(videoId);
        List<Comment> commentList = video.getComments();

        return commentList.stream().map(this::mapToCommentDto).toList();
    }

    private CommentDto mapToCommentDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setCommentText(comment.getText());
        commentDto.setAuthor(comment.getAuthor());
        return commentDto;
    }

    public List<VideoDto> getAllVideos() {
        return videoRepository.findAll().stream().map(this::mapToVideoDto).toList();
    }
}
