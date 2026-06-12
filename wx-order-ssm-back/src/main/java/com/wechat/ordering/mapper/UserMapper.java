package com.wechat.ordering.mapper;

import com.wechat.ordering.entity.User;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface UserMapper {
    List<User> selectAll();
    User selectById(Long id);
    User selectByOpenId(@Param("openId") String openId);
    int insert(User user);
    int update(User user);
    int deleteById(Long id);
}
