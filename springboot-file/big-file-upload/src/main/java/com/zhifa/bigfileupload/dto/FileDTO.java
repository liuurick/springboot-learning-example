package com.zhifa.bigfileupload.dto;

import lombok.Data;

/**
 * @author liubin
 */
@Data
public class FileDTO {

    private String key;
    private String fileName;
    private Long shardIndex;
    private Long shardSize;
    private Long shardTotal;
    private Long size;
    private String suffix;

}
