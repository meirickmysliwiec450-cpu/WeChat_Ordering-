package com.wechat.ordering.service.impl;

import com.wechat.ordering.entity.Banner;
import com.wechat.ordering.mapper.BannerMapper;
import com.wechat.ordering.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BannerServiceImpl implements BannerService {

    @Autowired
    private BannerMapper bannerMapper;

    @Override
    public List<Banner> list() {
        return bannerMapper.selectAll();
    }

    @Override
    public Banner getById(Long id) {
        return bannerMapper.selectById(id);
    }

    @Override
    public void add(Banner banner) {
        banner.setCreateTime(LocalDateTime.now());
        if (banner.getStatus() == null) banner.setStatus(1);
        bannerMapper.insert(banner);
    }

    @Override
    public void update(Banner banner) {
        bannerMapper.update(banner);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null) {
            throw new RuntimeException("轮播图不存在");
        }
        banner.setStatus(status);
        bannerMapper.update(banner);
    }

    @Override
    public void delete(Long id) {
        bannerMapper.deleteById(id);
    }
}
