package com.jiaocai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jiaocai.entity.Textbook;
import java.util.List;

public interface TextbookService extends IService<Textbook> {
    List<Textbook> search(String keyword, String category, String type, Double minPrice, Double maxPrice, String sellerId, String status);

    void createTextbook(Textbook textbook);
}
