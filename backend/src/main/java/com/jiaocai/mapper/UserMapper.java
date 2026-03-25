package com.jiaocai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiaocai.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
