package com.jiaocai.config;

import com.jiaocai.entity.Textbook;
import com.jiaocai.entity.User;
import com.jiaocai.service.TextbookService;
import com.jiaocai.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserService userService, TextbookService textbookService) {
        return args -> {
            if (userService.count() == 0) {
                User user = new User();
                user.setName("Test Student");
                user.setPhone("13800138000");
                user.setPassword("123456");
                user.setRole("student");
                user.setEmail("student@example.com");
                user.setBalance(1000.0);
                userService.save(user);
                System.out.println("Created test user: 13800138000 / 123456");
                
                User seller = new User();
                seller.setName("Test Seller");
                seller.setPhone("13900139000");
                seller.setPassword("123456");
                seller.setRole("student");
                seller.setBalance(500.0);
                userService.save(seller);

                if (textbookService.count() == 0) {
                    Textbook t1 = new Textbook();
                    t1.setTitle("算法导论");
                    t1.setAuthor("托马斯·科尔曼");
                    t1.setIsbn("9780262033848");
                    t1.setPrice(85.0);
                    t1.setCondition("良好");
                    t1.setCategory("计算机");
                    t1.setStatus("available");
                    t1.setSellerId(seller.getId());
                    t1.setCreatedAt(LocalDateTime.now());
                    textbookService.save(t1);

                    Textbook t2 = new Textbook();
                    t2.setTitle("代码整洁之道");
                    t2.setAuthor("罗伯特·C·马丁");
                    t2.setIsbn("9780132350884");
                    t2.setPrice(45.0);
                    t2.setCondition("全新");
                    t2.setCategory("计算机");
                    t2.setStatus("available");
                    t2.setSellerId(seller.getId());
                    t2.setCreatedAt(LocalDateTime.now());
                    textbookService.save(t2);
                    
                    System.out.println("Created test textbooks");
                }
            }
        };
    }
}
