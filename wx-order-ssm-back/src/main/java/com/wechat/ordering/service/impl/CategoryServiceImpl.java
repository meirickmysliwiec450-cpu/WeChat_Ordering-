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
        List<Category> list = categoryMapper.selectAll();
        // 按排序号升序，排序号相同的按ID升序（新分类在下面）
        list.sort((a, b) -> {
            int sa = a.getSort() != null ? a.getSort() : 0;
            int sb = b.getSort() != null ? b.getSort() : 0;
            if (sa != sb) return Integer.compare(sa, sb);
            return Long.compare(a.getId(), b.getId());
        });
        return list;
    }

    @Override
    public Category getById(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public void add(Category category) {
        category.setCreateTime(LocalDateTime.now());
        // 如果未指定排序号，自动设为当前最大排序号+1（新分类排在最后）
        if (category.getSort() == null || category.getSort() == 0) {
            List<Category> all = categoryMapper.selectAll();
            int maxSort = all.stream().mapToInt(c -> c.getSort() != null ? c.getSort() : 0).max().orElse(0);
            category.setSort(maxSort + 1);
        }
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
