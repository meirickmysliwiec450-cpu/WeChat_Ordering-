package com.wechat.ordering.service.impl;

import com.wechat.ordering.entity.User;
import com.wechat.ordering.mapper.UserMapper;
import com.wechat.ordering.service.WxUserService;
import com.wechat.ordering.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WxUserServiceImpl implements WxUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Map<String, Object> login(String openId, String nickName, String avatar, Integer gender) {
        User user = userMapper.selectByOpenId(openId);
        if (user == null) {
            // 新用户注册
            user = User.builder()
                    .openId(openId)
                    .nickName(nickName)
                    .avatar(avatar)
                    .gender(gender != null ? gender : 0)
                    .status(1)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
            user = userMapper.selectByOpenId(openId); // 获取自增ID
        } else {
            // 老用户更新信息
            if (nickName != null) user.setNickName(nickName);
            if (avatar != null) user.setAvatar(avatar);
            userMapper.update(user);
        }

        String token = JwtUtil.generateForUser(user.getId(), user.getOpenId());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    @Override
    public User getInfo(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public void updateInfo(Long userId, User user) {
        User exist = userMapper.selectById(userId);
        if (exist == null) throw new RuntimeException("用户不存在");
        if (user.getNickName() != null) exist.setNickName(user.getNickName());
        if (user.getAvatar() != null) exist.setAvatar(user.getAvatar());
        if (user.getGender() != null) exist.setGender(user.getGender());
        if (user.getPhone() != null) exist.setPhone(user.getPhone());
        userMapper.update(exist);
    }
}
