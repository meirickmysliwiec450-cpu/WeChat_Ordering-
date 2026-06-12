package com.wechat.ordering.service.impl;

import com.wechat.ordering.entity.Category;
import com.wechat.ordering.mapper.CategoryMapper;
import com.wechat.ordering.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> list() {
        return categoryMapper.selectAll();
    }

    @Override
    public Category getById(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public void add(Category category) {
        category.setCreateTime(LocalDateTime.now());
        categoryMapper.insert(category);
    }

    @Override
    public void update(Category category) {
        categoryMapper.update(category);
    }

    @Override
    public void delete(Long id) {
        categoryMapper.deleteById(id);
    }
}
