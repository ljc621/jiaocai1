package com.jiaocai.controller;

import com.jiaocai.common.Result;
import com.jiaocai.entity.Textbook;
import com.jiaocai.service.TextbookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/textbooks")
@CrossOrigin
public class TextbookController {

    @Autowired
    private TextbookService textbookService;

    @GetMapping
    public Result<List<Textbook>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sellerId,
            @RequestParam(required = false) String status) {
        try {
            return Result.success(textbookService.search(keyword, category, type, minPrice, maxPrice, sellerId, status));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("Failed to fetch textbooks: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Textbook> getById(@PathVariable String id) {
        return Result.success(textbookService.getById(id));
    }

    @PostMapping
    public Result<Textbook> create(@RequestBody Textbook textbook) {
        try {
            textbook.setStatus("available");
            textbook.setCreatedAt(LocalDateTime.now());
            textbook.setUpdatedAt(LocalDateTime.now());
            textbookService.createTextbook(textbook);
            return Result.success(textbook);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("Failed to create textbook: " + e.getMessage());
        }
    }

    @PutMapping
    public Result<Textbook> update(@RequestBody Textbook textbook) {
        textbook.setUpdatedAt(LocalDateTime.now());
        textbookService.updateById(textbook);
        return Result.success(textbook);
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
        textbookService.removeById(id);
        return Result.success("Textbook deleted");
    }
}
