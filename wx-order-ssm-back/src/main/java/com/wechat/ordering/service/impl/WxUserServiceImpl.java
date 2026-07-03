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
        System.out.println("  [WxUserService] 开始处理登录，openId: " + openId);
        
        User user = userMapper.selectByOpenId(openId);
        
        if (user == null) {
            System.out.println("  [WxUserService] 新用户，准备注册...");
            // 新用户注册
            user = User.builder()
                    .openId(openId)
                    .nickName(nickName)
                    .avatar(avatar)
                    .gender(gender != null ? gender : 0)
                    .status(1)
                    .createTime(LocalDateTime.now())
                    .build();
            
            System.out.println("  [WxUserService] 准备插入用户数据：" + user);
            int insertResult = userMapper.insert(user);
            System.out.println("  [WxUserService] 插入结果影响行数：" + insertResult);
            
            user = userMapper.selectByOpenId(openId); // 获取自增ID
            System.out.println("  [WxUserService] 注册成功！新用户ID: " + (user != null ? user.getId() : "null"));
        } else {
            System.out.println("  [WxUserService] 老用户登录，用户ID: " + user.getId());
            // 老用户更新信息
            if (nickName != null) user.setNickName(nickName);
            if (avatar != null) user.setAvatar(avatar);
            userMapper.update(user);
        }

        String token = JwtUtil.generateForUser(user.getId(), user.getOpenId());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        
        System.out.println("  [WxUserService] 登录处理完成！");
        System.out.println("  [WxUserService] 用户ID: " + user.getId());
        System.out.println("  [WxUserService] 生成的Token（前50字符）: " + (token.length() > 50 ? token.substring(0, 50) + "..." : token));
        System.out.println("  [WxUserService] 返回完整结果: " + result);
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
