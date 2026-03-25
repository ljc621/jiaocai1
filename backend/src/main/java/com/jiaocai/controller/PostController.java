package com.jiaocai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiaocai.common.Result;
import com.jiaocai.entity.Post;
import com.jiaocai.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping
    public Result<List<Post>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "newest") String sort) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Post::getType, type);
        }
        
        // Sort logic
        if ("replies".equals(sort)) {
            wrapper.orderByDesc(Post::getCommentsCount);
        } else if ("views".equals(sort)) {
            wrapper.orderByDesc(Post::getViews);
        } else {
            wrapper.orderByDesc(Post::getCreatedAt);
        }
        
        return Result.success(postService.list(wrapper));
    }

    @GetMapping("/{id}")
    public Result<Post> getById(@PathVariable Long id) {
        Post post = postService.getById(id);
        if (post != null) {
            // Increment views
            post.setViews(post.getViews() + 1);
            postService.updateById(post);
        }
        return Result.success(post);
    }

    @PostMapping
    public Result<Post> create(@RequestBody Post post) {
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setViews(0);
        post.setLikes(0);
        post.setCommentsCount(0);
        postService.save(post);
        return Result.success(post);
    }

    @PostMapping("/{id}/like")
    public Result<String> like(@PathVariable Long id) {
        Post post = postService.getById(id);
        if (post == null) {
            return Result.error("Post not found");
        }
        post.setLikes((post.getLikes() == null ? 0 : post.getLikes()) + 1);
        postService.updateById(post);
        return Result.success("Liked");
    }

    @PostMapping("/{id}/unlike")
    public Result<String> unlike(@PathVariable Long id) {
        Post post = postService.getById(id);
        if (post == null) {
            return Result.error("Post not found");
        }
        int likes = post.getLikes() == null ? 0 : post.getLikes();
        if (likes > 0) {
            post.setLikes(likes - 1);
            postService.updateById(post);
        }
        return Result.success("Unliked");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        postService.removeById(id);
        return Result.success("Post deleted");
    }
}
