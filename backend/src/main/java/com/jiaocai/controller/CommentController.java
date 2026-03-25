package com.jiaocai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiaocai.common.Result;
import com.jiaocai.entity.Comment;
import com.jiaocai.entity.Post;
import com.jiaocai.service.CommentService;
import com.jiaocai.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @GetMapping
    public Result<List<Comment>> list(@RequestParam Long postId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId);
        wrapper.orderByAsc(Comment::getCreatedAt);
        return Result.success(commentService.list(wrapper));
    }

    @PostMapping
    public Result<Comment> create(@RequestBody Comment comment) {
        comment.setCreatedAt(LocalDateTime.now());
        commentService.save(comment);
        
        // Update post comment count
        Post post = postService.getById(comment.getPostId());
        if (post != null) {
            post.setCommentsCount((post.getCommentsCount() == null ? 0 : post.getCommentsCount()) + 1);
            postService.updateById(post);
        }
        
        return Result.success(comment);
    }
}
