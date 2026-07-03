package com.wechat.ordering.controller;

import com.wechat.ordering.util.FileUploadUtil;
import com.wechat.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/common/upload")
public class CommonUploadController {
    @Autowired
    private FileUploadUtil fileUploadUtil;

    /**
     * 图片上传接口
     * bizType：comment评价 / avatar头像 / goods商品
     * 前端调用地址：POST /api/common/upload/image?bizType=comment
     */
    @PostMapping("/image")
    public Result<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bizType", defaultValue = "default") String bizType
    ) {
        try {
            String url = fileUploadUtil.uploadImage(file, bizType);
            return Result.success(url);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 通用文件上传（文档、视频等）
     */
    @PostMapping("/file")
    public Result<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bizType", defaultValue = "file") String bizType
    ) {
        try {
            String url = fileUploadUtil.upload(file, bizType);
            return Result.success(url);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
}