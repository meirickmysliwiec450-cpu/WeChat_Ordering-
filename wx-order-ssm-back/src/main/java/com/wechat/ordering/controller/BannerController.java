package com.wechat.ordering.controller;

import com.wechat.ordering.entity.Banner;
import com.wechat.ordering.service.BannerService;
import com.wechat.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/banners")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @GetMapping
    public Result<List<Banner>> list() {
        return Result.success(bannerService.list());
    }

    @GetMapping("/{id}")
    public Result<Banner> getById(@PathVariable Long id) {
        Banner banner = bannerService.getById(id);
        if (banner == null) {
            return Result.error("轮播图不存在");
        }
        return Result.success(banner);
    }

    @PostMapping
    public Result<?> add(@RequestBody Banner banner) {
        bannerService.add(banner);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Banner banner) {
        banner.setId(id);
        bannerService.update(banner);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        try {
            bannerService.updateStatus(id, params.get("status"));
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        bannerService.delete(id);
        return Result.success();
    }
}
