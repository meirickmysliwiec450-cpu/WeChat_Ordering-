package com.wechat.ordering.service.impl;

import com.wechat.ordering.entity.User;
import com.wechat.ordering.mapper.UserMapper;
import com.wechat.ordering.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Map<String, Object> list(Integer page, Integer pageSize) {
        List<User> all = userMapper.selectAll();
        int total = all.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<User> pageList = all.subList(Math.min(fromIndex, total), toIndex);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", pageList);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public void update(User user) {
        userMapper.update(user);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(status);
        userMapper.update(user);
    }
}
