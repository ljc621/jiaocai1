-- Users
INSERT INTO `User` (id, phone, password, name, university, balance, role, credit_score, verified) 
SELECT 'admin', '10086', '123456', '超级管理员', 'Platform Center', 999.00, 'admin', 999, TRUE
WHERE NOT EXISTS (SELECT 1 FROM `User` WHERE id = 'admin');

INSERT INTO `User` (id, phone, password, name, university, balance, role, credit_score, verified) 
SELECT 'user1', '13800138001', '123456', 'Alice', 'Tsinghua University', 500.00, 'student', 650, TRUE
WHERE NOT EXISTS (SELECT 1 FROM `User` WHERE id = 'user1');

INSERT INTO `User` (id, phone, password, name, university, balance, role, credit_score, verified) 
SELECT 'user2', '13900139002', '123456', 'Bob', 'Peking University', 300.00, 'student', 580, TRUE
WHERE NOT EXISTS (SELECT 1 FROM `User` WHERE id = 'user2');

-- Addresses
INSERT INTO `address` (id, user_id, recipient, phone, detail, is_default) 
SELECT 'addr1', 'user1', 'Alice', '13800138001', 'Building 5, Tsinghua University', true
WHERE NOT EXISTS (SELECT 1 FROM `address` WHERE id = 'addr1');

INSERT INTO `address` (id, user_id, recipient, phone, detail, is_default) 
SELECT 'addr2', 'user2', 'Bob', '13900139002', 'Dorm 3, Peking University', true
WHERE NOT EXISTS (SELECT 1 FROM `address` WHERE id = 'addr2');

-- Textbooks
INSERT INTO `Textbook` (id, title, author, isbn, price, original_price, `condition`, category, description, seller_id, status, cover, type, views, created_at) 
SELECT 'tb1', '算法导论', '托马斯·科尔曼', '9780262033848', 85.00, 120.00, '良好', '计算机', '经典算法书，轻微使用痕迹。', 'user2', 'available', 'https://m.media-amazon.com/images/I/61Mw06xul8L._AC_UF1000,1000_QL80_.jpg', 'sell', 102, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `Textbook` WHERE id = 'tb1');

INSERT INTO `Textbook` (id, title, author, isbn, price, original_price, `condition`, category, description, seller_id, status, cover, type, views, created_at) 
SELECT 'tb2', '代码整洁之道', '罗伯特·C·马丁', '9780132350884', 45.00, 60.00, '全新', '计算机', '开发者必读。', 'user2', 'available', 'https://m.media-amazon.com/images/I/51E2055ZGUL._AC_UF1000,1000_QL80_.jpg', 'sell', 88, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `Textbook` WHERE id = 'tb2');

INSERT INTO `Textbook` (id, title, author, isbn, price, original_price, `condition`, category, description, seller_id, status, cover, type, views, created_at) 
SELECT 'tb3', '微积分', '詹姆斯·斯图尔特', '9781285741550', 120.00, 200.00, '一般', '数学', '内有少量高亮标记。', 'user1', 'available', 'https://m.media-amazon.com/images/I/81I-W8-eNnL._AC_UF1000,1000_QL80_.jpg', 'sell', 56, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `Textbook` WHERE id = 'tb3');

INSERT INTO `Textbook` (id, title, author, isbn, price, original_price, `condition`, category, description, seller_id, status, cover, type, views, created_at) 
SELECT 'tb4', '大学物理', 'Serway', '9781133947271', 95.00, 150.00, '良好', '物理', '成色极佳。', 'user2', 'available', 'https://m.media-amazon.com/images/I/71pC69I3l-L._AC_UF1000,1000_QL80_.jpg', 'rent', 120, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `Textbook` WHERE id = 'tb4');

-- Posts
INSERT INTO posts (user_id, title, content, type, views, likes, comments_count, created_at, updated_at) 
SELECT 'user1', '如何鉴别正版教材？', '大家在买二手书的时候要注意...', 'discussion', 100, 10, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM posts WHERE title = '如何鉴别正版教材？');

INSERT INTO posts (user_id, title, content, type, views, likes, comments_count, created_at, updated_at) 
SELECT 'user2', '求购《计算机网络》第七版', '急需一本，有的请联系', 'transaction', 50, 2, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM posts WHERE title = '求购《计算机网络》第七版');

INSERT INTO posts (user_id, title, content, type, views, likes, comments_count, created_at, updated_at) 
SELECT 'user1', '算法导论阅读心得', '这本书真的很难啃...', 'review', 200, 20, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM posts WHERE title = '算法导论阅读心得');

-- Comments
INSERT INTO comments (post_id, user_id, content, created_at) 
SELECT 1, 'user2', '感谢分享！', NOW()
WHERE NOT EXISTS (SELECT 1 FROM comments WHERE id = 1);

INSERT INTO comments (post_id, user_id, content, created_at) 
SELECT 2, 'user1', '我有一本，怎么联系？', NOW()
WHERE NOT EXISTS (SELECT 1 FROM comments WHERE id = 2);
