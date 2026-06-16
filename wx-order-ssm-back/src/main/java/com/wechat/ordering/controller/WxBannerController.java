package com.wechat.ordering.controller;

import com.wechat.ordering.entity.Banner;
import com.wechat.ordering.mapper.BannerMapper;
import com.wechat.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wx/banners")
public class WxBannerController {

    @Autowired
    private BannerMapper bannerMapper;

    /** 获取启用的轮播图列表 */
    @GetMapping
    public Result<List<Banner>> list() {
        return Result.success(bannerMapper.selectByStatus(1));
    }
}
