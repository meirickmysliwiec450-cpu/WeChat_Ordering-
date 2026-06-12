package com.wechat.ordering.mapper;

import com.wechat.ordering.entity.Category;
import java.util.List;

public interface CategoryMapper {
    List<Category> selectAll();
    Category selectById(Long id);
    int insert(Category category);
    int update(Category category);
    int deleteById(Long id);
}
