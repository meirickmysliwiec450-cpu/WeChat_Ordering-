package com.wechat.ordering.controller;

import com.wechat.ordering.config.AppConfig;
import com.wechat.ordering.util.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 图片管理接口 - 列出前端 public/images 目录下的图片文件 + 文件上传
 */
@RestController
@RequestMapping("/admin/upload")
public class UploadController {

    private static final String IMAGES_DIR = "E:/wechat_images";
    private static final String AVATAR_DIR = "E:/wechat_images/avatars";

    /** 上传单张图片（用于头像上传等场景） */
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error("文件不能为空");
        try {
            // 确保目录存在
            File dir = new File(AVATAR_DIR);
            if (!dir.exists()) dir.mkdirs();

            // 生成唯一文件名
            String ext = "png";
            String originalName = file.getOriginalFilename();
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf(".") + 1);
            }
            String fileName = "avatar_" + System.currentTimeMillis() + "." + ext;
            File dest = new File(dir, fileName);

            file.transferTo(dest);

            String relativePath = "/images/avatars/" + fileName;
            Map<String, String> result = new HashMap<>();
            result.put("url", AppConfig.resolveImage(relativePath));
            result.put("path", relativePath);
            return Result.success(result);
        } catch (IOException e) {
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    /** 获取所有可用图片列表（递归扫描子目录） */
    @GetMapping("/images")
    public Result<List<Map<String, String>>> listImages() {
        File root = new File(IMAGES_DIR);
        List<Map<String, String>> result = new ArrayList<>();
        scanDir(root, "", result);
        return Result.success(result);
    }

    private void scanDir(File dir, String prefix, List<Map<String, String>> result) {
        if (!dir.exists() || !dir.isDirectory()) return;
        // 跳过用户头像目录，只显示菜品相关图片
        if (dir.getName().equals("avatars")) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                scanDir(f, prefix + f.getName() + "/", result);
            } else {
                String name = f.getName().toLowerCase();
                if (name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                    name.endsWith(".png") || name.endsWith(".gif") ||
                    name.endsWith(".webp")) {
                    Map<String, String> item = new HashMap<>();
                    item.put("name", prefix + f.getName());
                    item.put("path", "/images/" + prefix + f.getName());  // 相对路径，推荐存入数据库
                    item.put("url", AppConfig.resolveImage("/images/" + prefix + f.getName()));
                    result.add(item);
                }
            }
        }
    }
}
