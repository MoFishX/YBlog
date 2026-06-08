package com.yvmoux.blog.utils;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static com.qiniu.storage.Region.createWithRegionId;

@Data
@AllArgsConstructor
@Slf4j
public class QiNiuOssUtil {

    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    public String upload(byte[] bytes, String objectName) {
        // 创建 文件路径 和 文件名
        objectName = "_yvmou/blog/" + objectName;

        // 创建OSSClient实例。
        Auth auth = Auth.create(accessKeyId, accessKeySecret);

        // 创建Token
        String upToken = auth.uploadToken(bucketName, objectName);

        // 创建上传对象
        Configuration cfg = Configuration.create(createWithRegionId("z2"));
        UploadManager uploadManager = new UploadManager(cfg);

        try {
            Response response = uploadManager.put(bytes, objectName, upToken);
        } catch (QiniuException e) {
            throw new RuntimeException(e);
        }

        //文件访问路径
        String url = "https://cdn.yvmou.cn/" + objectName;
        log.info("文件上传到:{}", url);

        return url;
    }
}
