package com.star.service;

import com.star.domain.Chunk;
import com.star.domain.ChunkResult;
import com.star.util.FileUtils;
import com.star.util.RedisUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * @Author: zzStar
 * @Date: 03-20-2022 22:23
 */
@Service
public class UploadService {

    private Logger log = LoggerFactory.getLogger(UploadService.class);

    @Resource
    private RedisUtils redisUtils;
    @Resource
    private FileUtils fileUtils;

    @Value("${upload-folder}")
    private String uploadFolder;

    /**
     * 检查文件是否存在，如果存在则跳过该文件的上传，如果不存在，返回需要上传的分片集合
     *
     * @param chunk Chunk
     * @return ChunkResult
     */
    public ChunkResult checkChunkExist(Chunk chunk) {
        String folderPath = fileUtils.getFileFolderPath(chunk.getIdentifier());
        log.info("folderPath: {}", folderPath);
        String filePath = fileUtils.getFilePath(chunk.getIdentifier(), chunk.getFilename());
        log.info("filePath: {}", filePath);
        File file = new File(filePath);
        boolean exists = file.exists();
        Set<Integer> uploaded = (Set<Integer>) redisUtils.hGet(chunk.getIdentifier(), "uploaded");
        if (Objects.nonNull(uploaded) && uploaded.size() == chunk.getTotalChunks() && exists) {
            return new ChunkResult(true);
        }
        File folder = new File(folderPath);
        if (!folder.exists()) {
            boolean mkdirs = folder.mkdirs();
            log.info("准备工作, 创建文件夹, folderPath: {}, mkdirs: {}", folderPath, mkdirs);
        }
        return new ChunkResult(false, uploaded);
    }

    /**
     * 上传分片
     *
     * @param chunk 分片
     */
    public void uploadChunk(Chunk chunk) {
        String folderPath = fileUtils.getChunkFileFolderPath(chunk.getIdentifier());
        File chunkFolder = new File(folderPath);
        if (!chunkFolder.exists()) {
            boolean mkdirs = chunkFolder.mkdirs();
            log.info("创建分片文件夹: {}", mkdirs);
        }
        try (
                InputStream in = chunk.getFile().getInputStream();
                FileOutputStream os = new FileOutputStream(
                        new File(folderPath + chunk.getChunkNumber()))
        ) {
            IOUtils.copy(in, os);
            log.info("文件标识: {}, chunkNumber: {}", chunk.getIdentifier(), chunk.getChunkNumber());
            putRedis(chunk);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    synchronized void putRedis(Chunk chunk) {
        Set<Integer> uploaded = (Set<Integer>) redisUtils.hGet(chunk.getIdentifier(), "uploaded");
        if (Objects.isNull(uploaded)) {
            uploaded = new HashSet<>(Arrays.asList(chunk.getChunkNumber()));
            Map<String, Object> map = new HashMap<>();
            map.put("uploaded", uploaded);
            map.put("totalChunk", chunk.getTotalChunks());
            map.put("totalSize", chunk.getTotalSize());
            map.put("path", chunk.getFilename());
            redisUtils.hSetAll(chunk.getIdentifier(), map);
        } else {
            uploaded.add(chunk.getChunkNumber());
            redisUtils.hSet(chunk.getIdentifier(), "uploaded", uploaded);
        }
    }

    public boolean mergeChunk(String md5, String filename, Integer totalChunks) {
        String folderPath = fileUtils.getChunkFileFolderPath(md5);
        String filePath = fileUtils.getFilePath(md5, filename);
        if (checkChunk(folderPath, totalChunks)) {
            File chunkFileFolder = new File(folderPath);
            File mergeFile = new File(filePath);
            File[] chunks = chunkFileFolder.listFiles();
            List<File> fileList = Arrays.asList(chunks);
            log.info("fileList: " + fileList);
            Collections.sort(fileList, (Comparator<File>) Comparator.comparingInt((File o) -> Integer.parseInt(o.getName())));
            try {
                RandomAccessFile rw = new RandomAccessFile(mergeFile, "rw");
                byte[] bytes = new byte[1024];
                for (File chunk : chunks) {
                    RandomAccessFile r = new RandomAccessFile(chunk, "r");
                    int len;
                    while ((len = r.read(bytes)) != -1) {
                        rw.write(bytes, 0, len);
                    }
                    r.close();
                }
                rw.close();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean checkChunk(String folderPath, Integer totalChunk) {
        try {
            for (int i = 1; i <= totalChunk + 1; i++) {
                File file = new File(folderPath + File.separator + i);
                if (file.exists()) {
                    continue;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean mergeChunks(String md5, String fileName, Integer totalChunks) {
        return mergeChunk(md5, fileName, totalChunks);
    }

}
