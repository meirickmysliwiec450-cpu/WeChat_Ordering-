package com.wechat.ordering.service;

import com.wechat.ordering.entity.Banner;
import java.util.List;

public interface BannerService {
    List<Banner> list();
    Banner getById(Long id);
    void add(Banner banner);
    void update(Banner banner);
    void updateStatus(Long id, Integer status);
    void delete(Long id);
}
