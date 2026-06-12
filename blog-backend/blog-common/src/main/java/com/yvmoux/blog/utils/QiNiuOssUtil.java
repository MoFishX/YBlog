package com.yvmoux.blog.utils;

import com.qiniu.common.QiniuException;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.yvmoux.blog.config.StorageConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.qiniu.storage.Region.createWithRegionId;

@Slf4j
@Component
@RequiredArgsConstructor
public class QiNiuOssUtil {

    private final StorageConfig storageConfig;

    public String upload(byte[] bytes, String objectName) {
        // 创建 文件路径 和 文件名
        String pathPrefix = storageConfig.getOss().getPathPrefix()
                .replaceAll("/+$", "");
        objectName = pathPrefix + "/" + objectName;

        // 创建OSSClient实例。
        Auth auth = Auth.create(storageConfig.getOss().getAccessKeyId(), storageConfig.getOss().getAccessKeySecret());

        // 创建Token
        String upToken = auth.uploadToken(storageConfig.getOss().getBucketName(), objectName);

        // 创建上传对象
        Configuration cfg = Configuration.create(createWithRegionId("z2"));
        UploadManager uploadManager = new UploadManager(cfg);

        try {
            uploadManager.put(bytes, objectName, upToken);
        } catch (QiniuException e) {
            throw new RuntimeException(e);
        }

        String url = storageConfig.getOss().getEndpoint() + "/" + objectName;
        log.info("文件上传到:{}", url);

        return url;
    }
}