package com.jiaocai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiaocai.common.Result;
import com.jiaocai.entity.Review;
import com.jiaocai.entity.Textbook;
import com.jiaocai.entity.User;
import com.jiaocai.service.ReviewService;
import com.jiaocai.service.TextbookService;
import com.jiaocai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @Autowired
    private TextbookService textbookService;

    @GetMapping("/textbook/{id}")
    public Result<List<Review>> listByTextbook(@PathVariable String id) {
        LambdaQueryWrapper<Review> query = new LambdaQueryWrapper<>();
        query.eq(Review::getTextbookId, id);
        query.orderByDesc(Review::getCreatedAt);
        List<Review> reviews = reviewService.list(query);
        fillUserInfo(reviews);
        return Result.success(reviews);
    }

    @GetMapping("/seller/{id}")
    public Result<List<Review>> listBySeller(@PathVariable String id) {
        LambdaQueryWrapper<Review> query = new LambdaQueryWrapper<>();
        query.eq(Review::getSellerId, id);
        query.orderByDesc(Review::getCreatedAt);
        List<Review> reviews = reviewService.list(query);
        fillUserInfo(reviews);
        return Result.success(reviews);
    }

    private void fillUserInfo(List<Review> reviews) {
        for (Review review : reviews) {
            User user = userService.getById(review.getUserId());
            if (user != null) {
                review.setUserName(user.getName());
                review.setUserAvatar(user.getAvatar());
            } else {
                review.setUserName("未知用户");
            }
        }
    }

    @PostMapping
    public Result<Review> create(@RequestBody Review review) {
        review.setCreatedAt(LocalDateTime.now());
        reviewService.save(review);

        // Update seller credit score based on rating
        if (review.getRating() != null && review.getTextbookId() != null) {
            Textbook textbook = textbookService.getById(review.getTextbookId());
            if (textbook != null) {
                User seller = userService.getById(textbook.getSellerId());
                if (seller != null) {
                    int currentScore = seller.getCreditScore() == null ? 600 : seller.getCreditScore();
                    if (review.getRating() >= 4) {
                        seller.setCreditScore(currentScore + 2); // Less impact for direct reviews
                    } else if (review.getRating() <= 2) {
                        seller.setCreditScore(currentScore - 5);
                    }
                    userService.updateById(seller);
                }
            }
        }

        return Result.success(review);
    }

    @GetMapping("/admin/list")
    public Result<List<Review>> listAll() {
        LambdaQueryWrapper<Review> query = new LambdaQueryWrapper<>();
        query.orderByDesc(Review::getCreatedAt);
        List<Review> reviews = reviewService.list(query);
        fillUserInfo(reviews);
        return Result.success(reviews);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        reviewService.removeById(id);
        return Result.success(null);
    }
}
