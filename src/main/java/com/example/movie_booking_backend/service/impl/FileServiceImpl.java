package com.example.movie_booking_backend.service.impl;



import com.example.movie_booking_backend.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMM");

    @Value("${file-upload-path}")
    private String fileUploadPath;

    @Override
    public Map<String, String> uploadFile(MultipartFile file, String subDir) throws IOException {
        String targetPath = Paths.get(fileUploadPath, subDir).toString();
        return storeFile(file, targetPath, subDir);
    }

    @Override
    public Map<String, String> uploadAvatar(MultipartFile file, String subDir, Long userId) throws IOException {
        // 1. 验证文件
        if (file.isEmpty()) {
            throw new IOException("头像文件不能为空");
        }

        // 2. 验证文件类型
        String contentType = file.getContentType();
        if (!Arrays.asList("image/jpeg", "image/png").contains(contentType)) {
            throw new IOException("只支持JPEG/PNG格式的图片");
        }

        // 3. 创建目标目录
        String targetPath = Paths.get(fileUploadPath, subDir).toString();
        File dir = new File(targetPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 4. 获取上传前的用户头像列表（重点！）
        Set<String> oldFiles = getCurrentUserFiles(dir, userId);

        // 5. 生成新文件名（使用时间戳，保持原有命名规则）
        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf("."));
        String newFileName = "user_" + userId + "_" + System.currentTimeMillis() + extension;
        String filePath = targetPath + File.separator + newFileName;

        // 6. 保存新文件
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            out.write(file.getBytes());
        }

        // 7. 获取上传后的用户头像列表，并删除旧文件
        deleteOldFiles(dir, userId, oldFiles, newFileName);

        // 8. 返回相对路径
        Map<String, String> result = new HashMap<>();
        result.put("url", "/" + subDir + "/" + newFileName);
        log.info("头像上传成功: {}", result.get("url"));
        return result;
    }

    // 获取当前目录下所有用户的头像文件
    private Set<String> getCurrentUserFiles(File dir, Long userId) {
        Set<String> userFiles = new HashSet<>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                // 匹配 user_{userId}_*.{扩展名}
                if (fileName.startsWith("user_" + userId + "_") &&
                        (fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".jpeg"))) {
                    userFiles.add(fileName);
                }
            }
        }
        return userFiles;
    }

    // 删除旧文件（根据上传前后的文件列表对比）
    private void deleteOldFiles(File dir, Long userId, Set<String> oldFiles, String newFileName) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.startsWith("user_" + userId + "_") &&
                        (fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".jpeg")) &&
                        !fileName.equals(newFileName)) {
                    boolean deleted = file.delete();
                    if (deleted) {
                        log.info("删除旧头像文件: {}", file.getAbsolutePath());
                    } else {
                        log.warn("无法删除旧头像文件: {}", file.getAbsolutePath());
                        file.setWritable(true);
                        if (file.delete()) {
                            log.info("强制删除旧头像文件: {}", file.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    private static Map<String, String> storeFile(MultipartFile file, String fileUploadPath, String subDir) throws IOException {
        String yearMonth = SDF.format(new Date());
        String fileName = file.getOriginalFilename();
        String suffix = suffix(fileName);
        String relPath = "/" + yearMonth + "/" + UUID.randomUUID().toString().replaceAll("-","") + suffix;
        String toPath = fileUploadPath + relPath;
        FileOutputStream out = null;
        File toFile = new File(toPath).getParentFile();
        if (!toFile.exists()) {
            toFile.mkdirs();
        }
        try  {
            out = new FileOutputStream(toPath);
            out.write(file.getBytes());
            out.flush();
            Map<String, String> map = new HashMap<>();
            map.put("url", "./" + subDir + relPath);
            log.info(relPath);
            return map;
        } catch (FileNotFoundException fnfe) {
            throw fnfe;
        } catch (IOException ioe) {
            throw ioe;
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
// ... existing code ...
    /**
     * 后缀名或empty："a.png" => ".png"
     */
    private static String suffix(String fileName) {
        int i = fileName.lastIndexOf('.');
        return i == -1 ? "" : fileName.substring(i);
    }
}
