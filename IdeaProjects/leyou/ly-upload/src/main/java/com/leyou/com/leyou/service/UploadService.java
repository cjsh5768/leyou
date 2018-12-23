package com.leyou.com.leyou.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.com.leyou.controller.UploadController;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class UploadService {
    private static final Logger logger= LoggerFactory.getLogger(UploadController.class);
    private static final List<String> suffixes = Arrays.asList("image/png","image/jpeg");
    @Autowired
    FastFileStorageClient storageClient;

    public String upload(MultipartFile file) {
        try {
            String type = file.getContentType();
            if (!suffixes.contains(type)){
                logger.info("上传失败，文件类型{}不匹配",type);
                return null;
            }
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image==null){
                logger.info("上传失败，文件内容不符合要求");
                return null;
            }
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(),".");
            StorePath storePath = this.storageClient.uploadFile(file.getInputStream(),file.getSize(),extension,null);
            return "http://image.leyou.com/"+storePath.getFullPath();
        } catch (Exception e) {
            return null;
        }
    }
}
