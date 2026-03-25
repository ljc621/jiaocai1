package com.jiaocai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaocai.entity.Textbook;
import com.jiaocai.mapper.TextbookMapper;
import com.jiaocai.service.TextbookService;
import com.jiaocai.service.WalletTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.List;

@Service
public class TextbookServiceImpl extends ServiceImpl<TextbookMapper, Textbook> implements TextbookService {

    @Autowired
    private TextbookMapper textbookMapper;

    @Autowired
    private WalletTransactionService walletTransactionService;

    @Override
    public TextbookMapper getBaseMapper() {
        return textbookMapper;
    }

    @Override
    @Transactional
    public void createTextbook(Textbook textbook) {
        this.save(textbook);
        
        // 如果是爱心捐赠，奖励用户积分
        if ("donate".equals(textbook.getType())) {
            // 奖励100积分
            walletTransactionService.rewardPoints(textbook.getSellerId(), 100, "发布爱心捐赠奖励: " + textbook.getTitle());
        }
    }

    @Override
    public List<Textbook> search(String keyword, String category, String type, Double minPrice, Double maxPrice, String sellerId, String status) {
        LambdaQueryWrapper<Textbook> query = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            query.and(w -> {
                w.like(Textbook::getTitle, keyword)
                 .or().like(Textbook::getAuthor, keyword);
                
                // Only search ISBN if keyword is long enough to reduce noise
                if (keyword.length() >= 3) {
                    w.or().like(Textbook::getIsbn, keyword);
                }
            });
        }
        
        if (StringUtils.hasText(category)) {
            query.eq(Textbook::getCategory, category);
        }

        if (StringUtils.hasText(type)) {
            if ("rent".equals(type)) {
                query.eq(Textbook::getRentEnabled, true);
            } else {
                query.eq(Textbook::getType, type);
            }
        }
        
        if (minPrice != null) {
            query.ge(Textbook::getPrice, minPrice);
        }
        
        if (maxPrice != null) {
            query.le(Textbook::getPrice, maxPrice);
        }

        if (StringUtils.hasText(sellerId)) {
            query.eq(Textbook::getSellerId, sellerId);
        }
        
        if (StringUtils.hasText(status)) {
            query.eq(Textbook::getStatus, status);
        } else if (!StringUtils.hasText(sellerId)) {
            // Only filter by available if not searching by sellerId (market view)
            // But allow rented items to be shown as well
            query.in(Textbook::getStatus, "available", "rented");
        }
        
        query.orderByDesc(Textbook::getCreatedAt);
        
        return this.list(query);
    }
}
