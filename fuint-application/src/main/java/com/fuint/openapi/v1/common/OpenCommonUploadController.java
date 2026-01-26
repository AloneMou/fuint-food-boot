package com.fuint.openapi.v1.common;

import cn.iocoder.yudao.framework.signature.core.annotation.ApiSignature;
import com.aliyun.oss.OSS;
import com.fuint.common.service.SettingService;
import com.fuint.common.util.AliyunOssUtil;
import com.fuint.common.util.CommonUtil;
import com.fuint.common.util.DateUtil;
import com.fuint.framework.exception.ServiceException;
import com.fuint.framework.pojo.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.ResourceUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.fuint.framework.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;

/**
 * @author Miao
 * @date 2026/1/26
 */
@Slf4j
@Validated
@Api(tags = "OpenApi-公共接口")
@RestController
@RequestMapping(value = "/api/v1/common")
public class OpenCommonUploadController {

    /**
     * 环境变量
     */
    @Resource
    private Environment env;

    @Resource
    private SettingService settingService;

    @ApiOperation(value = "后台上传文件")
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @CrossOrigin
    @ApiSignature
    public CommonResult<Map<String, String>> uploadFileLocal(MultipartFile file) {
        Map<String, String> resultMap = new HashMap<>();
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isEmpty(originalFilename)) {
            throw new ServiceException(BAD_REQUEST.getCode(), "上传文件不能为空");
        }
        String maxSizeStr = env.getProperty("images.upload.maxSize");
        // 默认限制2M
        float maxSize = 2;
        try {
            assert maxSizeStr != null;
            maxSize = Float.parseFloat(maxSizeStr);
        } catch (NumberFormatException e) {
            log.error("图片允许的大小设置不正确", e);
        }
        if (file.getSize() > (maxSize * 1024 * 1024)) {
            throw new ServiceException(BAD_REQUEST.getCode(), "上传的文件不能大于" + maxSize + "MB");
        }
        String fileType = file.getContentType();

        // 保存文件
        try {
            String fileName = saveFile(file);
            String baseImage = settingService.getUploadBasePath();
            String filePath = baseImage + fileName;
            String url = filePath;

            // 上传阿里云oss
            String mode = env.getProperty("aliyun.oss.mode");
            assert mode != null;
            if (mode.equals("1") && fileType.contains("image")) { // 检查是否开启上传
                String endpoint = env.getProperty("aliyun.oss.endpoint");
                String accessKeyId = env.getProperty("aliyun.oss.accessKeyId");
                String accessKeySecret = env.getProperty("aliyun.oss.accessKeySecret");
                String bucketName = env.getProperty("aliyun.oss.bucketName");
                String folder = env.getProperty("aliyun.oss.folder");
                String domain = env.getProperty("aliyun.oss.domain");
                OSS ossClient = AliyunOssUtil.getOSSClient(accessKeyId, accessKeySecret, endpoint);
                String pathRoot = env.getProperty("images.root");
                if (pathRoot == null || StringUtils.isEmpty(pathRoot)) {
                    pathRoot = ResourceUtils.getURL("classpath:").getPath();
                }
                File ossFile = new File(pathRoot + fileName);
                fileName = AliyunOssUtil.upload(ossClient, ossFile, bucketName, folder);
                filePath = domain + fileName;
                url = filePath;
            }

            resultMap.put("status", "success");
            resultMap.put("domain", baseImage);
            resultMap.put("filePath", filePath);
            resultMap.put("fileName", fileName);
            resultMap.put("state", "SUCCESS");
            resultMap.put("original", file.getOriginalFilename());
            resultMap.put("size", file.getSize() + "");
            resultMap.put("title", fileName);
            resultMap.put("type", file.getContentType());
            resultMap.put("url", url);
        } catch (Exception e) {
            throw new ServiceException(BAD_REQUEST.getCode(), "上传失败，请检查上传配置及权限");
        }

        return CommonResult.success(resultMap);
    }

    public String saveFile(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        assert fileName != null;
        String imageName = fileName.substring(fileName.lastIndexOf("."));
        String pathRoot = env.getProperty("images.root");
        if (pathRoot == null || StringUtils.isEmpty(pathRoot)) {
            pathRoot = ResourceUtils.getURL("classpath:").getPath();
        }
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");

        String baseImage = env.getProperty("images.path");
        String filePath = baseImage + DateUtil.formatDate(new Date(), "yyyyMMdd") + "/";

        String path = filePath + uuid + imageName;

        try {
            File tempFile = new File(pathRoot + path);
            if (!tempFile.getParentFile().exists()) {
                tempFile.getParentFile().mkdirs();
            }
            CommonUtil.saveMultipartFile(file, pathRoot + path);
        } catch (Exception e) {
            throw new Exception("上传失败，请检查目录是否可写");
        }

        return path;
    }
}
