package com.wechat.ordering.service;

import com.wechat.ordering.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> list();
    Category getById(Long id);
    void add(Category category);
    void update(Category category);
    void delete(Long id);
}
