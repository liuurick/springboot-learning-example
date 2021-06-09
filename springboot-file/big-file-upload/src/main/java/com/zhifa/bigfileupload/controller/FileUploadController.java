package com.zhifa.bigfileupload.controller;

import com.zhifa.bigfileupload.constance.FileConstance;
import com.zhifa.bigfileupload.domain.FileTb;
import com.zhifa.bigfileupload.dto.FileDTO;
import com.zhifa.bigfileupload.service.FileTbService;
import com.zhifa.bigfileupload.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@RestController
@Slf4j
public class FileUploadController {

    @Autowired
    private FileTbService fileTbService;

    @PostMapping(value = "/upload")
    public Result upload(@RequestParam(value = "file") MultipartFile file,
                         FileDTO fileDTO) throws Exception {
        File fullDir = new File(FileConstance.FILE_PATH);
        if (!fullDir.exists()) {
            fullDir.mkdir();
        }

        //uid 防止文件名重复,又可以作为文件的唯一标识
        String fullPath = FileConstance.FILE_PATH + fileDTO.getKey() + "." + fileDTO.getShardIndex();
        File dest = new File(fullPath);
        file.transferTo(dest);
        log.info("文件分片 {} 保存完成",fileDTO.getShardIndex());

        //开始保存索引分片信息，不存在就新加，存在就修改索引分片
        FileTb fileTb = FileTb.builder()
                .fKey(fileDTO.getKey())
                .fIndex(Math.toIntExact(fileDTO.getShardIndex()))
                .fTotal(Math.toIntExact(fileDTO.getShardTotal()))
                .fName(fileDTO.getFileName())
                .fSize(fileDTO.getSize())
                .build();

        if (fileTbService.isNotExist(fileDTO.getKey())) {
            fileTbService.saveFile(fileTb);
        }else {
            fileTbService.UpdateFile(fileTb);
        }

        if (fileDTO.getShardIndex().equals(fileDTO.getShardTotal())) {
            log.info("开始合并");
            merge(fileDTO);
            return Result.success(FileConstance.ACCESS_PATH + fileDTO.getFileName());
        }
        return Result.success();
    }

    public void merge(FileDTO fileDTO) throws Exception {
        Long shardTotal = fileDTO.getShardTotal();
        File newFile = new File(FileConstance.FILE_PATH + fileDTO.getFileName());
        if (newFile.exists()) {
            newFile.delete();
        }
        //文件追加写入
        FileOutputStream outputStream = new FileOutputStream(newFile, true);
        //分片文件
        FileInputStream fileInputStream = null;
        //每个分片设定为10M
        byte[] bytes = new byte[10 * 1024 * 1024];
        int len;
        try {
            for (int i = 0; i < shardTotal; i++) {
                // 读取第i个分片
                fileInputStream = new FileInputStream(new File(FileConstance.FILE_PATH + fileDTO.getKey() + "." + (i + 1)));
                while ((len = fileInputStream.read(bytes)) != -1) {
                    //一直追加到合并的新文件中
                    outputStream.write(bytes, 0, len);
                }
            }
        } catch (IOException e) {
            log.error("分片合并异常", e);
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                outputStream.close();
                log.info("IO流关闭");
                System.gc();
            } catch (Exception e) {
                log.error("IO流关闭", e);
            }
        }
        log.info("合并分片结束");

        //告诉java虚拟机去回收垃圾 至于什么时候回收  这个取决于 虚拟机的决定
        System.gc();
        //等待100毫秒 等待垃圾回收去 回收完垃圾
        Thread.sleep(100);
        log.info("删除分片开始");
        for (int i = 0; i < shardTotal; i++) {
            String filePath = FileConstance.FILE_PATH + fileDTO.getKey() + "." + (i + 1);
            File file = new File(filePath);
            boolean result = file.delete();
            log.info("删除{}，{}", filePath, result ? "成功" : "失败");
        }
        log.info("删除分片结束");
    }

    /**
     * 文件上传之前判断是否已经上传过 -1就是没有
     * @param key
     * @return
     */
    @GetMapping("/check")
    public Result check(@RequestParam String key){
        FileTb fileTb = fileTbService.selectLatestIndex(key);
        log.info("检查分片：{}");
        return Result.success(fileTb);
    }

}
