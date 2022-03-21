package com.star.rest;

import com.star.domain.Chunk;
import com.star.domain.ChunkResult;
import com.star.service.UploadService;
import com.star.util.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Author: zzStar
 * @Date: 03-20-2022 22:15
 */
@RestController
@RequestMapping("upload")
public class UploadController {

    @Resource
    private UploadService uploadService;

    @GetMapping("chunk")
    public Result<?> checkChunkExist(Chunk chunk) {
        ChunkResult ret;
        try {
            ret = uploadService.checkChunkExist(chunk);
            return Result.success(ret);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("chunk")
    public Result<?> uploadChunk(Chunk chunk) {
        try {
            uploadService.uploadChunk(chunk);
            return Result.success(chunk.getIdentifier());
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("merge")
    public Result<?> mergeChunk(@RequestBody Chunk chunk) {
        try {
            boolean ok = uploadService.mergeChunks(chunk.getIdentifier(), chunk.getFilename(), chunk.getTotalChunks());
            return Result.success(ok);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

}
