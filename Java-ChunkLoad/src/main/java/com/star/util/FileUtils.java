package com.star.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @Author: zzStar
 * @Date: 03-20-2022 22:29
 */
@Component
public class FileUtils {

    @Value("${upload-folder}")
    public String uploadFolder;

    /**
     * 得到分块文件所属的目录
     *
     * @param identifier md5
     * @return String
     */
    public String getChunkFileFolderPath(String identifier) {
        return getFileFolderPath(identifier) + "chunks" + File.separator;
    }

    /**
     * 得到文件所属的目录
     *
     * @param identifier md5
     * @return String
     */
    public String getFileFolderPath(String identifier) {
        return uploadFolder + identifier.charAt(0) + File.separator +
                identifier.charAt(1) + File.separator +
                identifier + File.separator;
    }

    /**
     * 得到文件的相对路径
     *
     * @param identifier md5
     * @param filename   name
     * @return String
     */
    public String getFileRelativelyPath(String identifier, String filename) {
        String ext = filename.substring(filename.lastIndexOf("."));
        return "/" + identifier.charAt(0) + "/" +
                identifier.charAt(1) + "/" +
                identifier + "/" + identifier
                + ext;
    }

    /**
     * 得到文件的绝对路径
     *
     * @param identifier md5
     * @param filename   name
     * @return String
     */
    public String getFilePath(String identifier, String filename) {
        String ext = filename.substring(filename.lastIndexOf("."));
        return uploadFolder + filename;
    }

}
