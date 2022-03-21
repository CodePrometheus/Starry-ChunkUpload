package com.star.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: zzStar
 * @Date: 03-20-2022 22:14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chunk {

    /**
     * 文件md5
     */
    private String identifier;

    /**
     * 分块文件
     */
    MultipartFile file;

    /**
     * 当前分块序号
     */
    private Integer chunkNumber;

    /**
     * 分块大小
     */
    private Long chunkSize;

    /**
     * 当前分块大小
     */
    private Long currentChunkSize;

    /**
     * 文件总大小
     */
    private Long totalSize;

    /**
     * 分块总数
     */
    private Integer totalChunks;

    /**
     * 文件名
     */
    private String filename;

}
