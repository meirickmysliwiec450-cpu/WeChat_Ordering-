package com.wechat.ordering.util;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Component
public class FileUploadUtil {
    // 读取配置文件存储根路径
    @Value("${upload.local.root}")
    private String localRootPath;
    // 图片访问URL前缀
    @Value("${upload.visit.prefix}")
    private String visitPrefix;

    /**
     * 通用文件上传，bizType区分业务：comment评价、avatar头像、goods商品
     */
    public String upload(MultipartFile file, String bizType) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        String suffix = originalName.substring(originalName.lastIndexOf("."));
        // 唯一文件名
        String uuidName = UUID.randomUUID().toString().replace("-", "") + suffix;
        // 按业务+日期分目录
        String dateDir = new SimpleDateFormat("yyyyMMdd").format(new Date());
        File saveDir = new File(localRootPath, bizType + File.separator + dateDir);
        if (!saveDir.exists()) saveDir.mkdirs();
        File target = new File(saveDir, uuidName);
        file.transferTo(target);
        // 返回可访问地址
        return visitPrefix + "/" + bizType + "/" + dateDir + "/" + uuidName;
    }

    /**
     * 仅上传图片，限制格式
     */
    public String uploadImage(MultipartFile file, String bizType) throws Exception {
        String name = file.getOriginalFilename().toLowerCase();
        if (!name.endsWith(".jpg") && !name.endsWith(".png") && !name.endsWith(".jpeg")) {
            throw new RuntimeException("仅支持jpg/png/jpeg图片");
        }
        return upload(file, bizType);
    }
}
