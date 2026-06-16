package com.wechat.ordering.controller;

import com.wechat.ordering.util.Result;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.*;

/**
 * 图片管理接口 - 列出前端 public/images 目录下的图片文件
 */
@RestController
@RequestMapping("/admin/upload")
public class UploadController {

    private static final String IMAGES_DIR =
        System.getProperty("user.dir") + "/../wx-order-frontend-user1/public/images";

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
                    item.put("url", "/images/" + prefix + f.getName());
                    result.add(item);
                }
            }
        }
    }
}
