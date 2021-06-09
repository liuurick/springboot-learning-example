package com.liuurick.minio.controller;

import com.liuurick.minio.common.CommonResult;
import com.liuurick.minio.common.ResultCode;
import com.liuurick.minio.conf.MinioData;
import com.liuurick.minio.exception.CustomException;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.PutObjectOptions;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;

/**
 * minio上传,下载,删除接口
 */
@Api(tags = { "web服务：minio上传,下载,删除接口" })
@RestController
@RequestMapping("/minio")
public class MinioController {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioData minioData;

    /**
     * 下载文件
     */
    @ApiOperation(value = "下载文件")
    @GetMapping(value = "/download")
    @SneakyThrows(Exception.class)
    public void download(@RequestParam("fileName") String fileName, HttpServletResponse response) {
        InputStream in = null;
        final ObjectStat stat = minioClient.statObject(minioData.getBucketName(), fileName);
        response.setContentType(stat.contentType());
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        in = minioClient.getObject(minioData.getBucketName(), fileName);
        IOUtils.copy(in, response.getOutputStream());
        in.close();
    }

    /**
     * 上传文件
     * @param file
     * @return
     * @throws Exception ResponseMsg<String>
     */
    @ApiOperation(value = "上传文件")
    @PostMapping(value = "/upload")
    @SneakyThrows(Exception.class)
    public CommonResult<String> upload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new CustomException("上传文件不能为空");
        } else {
            // 得到文件流
            final InputStream is = file.getInputStream();
            // 文件名
            final String fileName = file.getOriginalFilename();
            // 把文件放到minio的boots桶里面
            minioClient.putObject(minioData.getBucketName(), fileName, is, new PutObjectOptions(is.available(), -1));
            // 关闭输入流
            is.close();
            return CommonResult.success("文件上传成功");
        }
    }

    /**
     * 删除文件
     * @param fileName
     * @return ResponseMsg<String>
     */
    @ApiOperation(value = "删除文件")
    @GetMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @SneakyThrows(Exception.class)
    public CommonResult<String> delete(@RequestParam("fileName") String fileName) {
        minioClient.removeObject(minioData.getBucketName(), fileName);
        return CommonResult.success("删除成功");
    }

}