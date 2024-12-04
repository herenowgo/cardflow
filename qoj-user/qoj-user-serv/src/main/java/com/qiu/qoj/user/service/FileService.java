package com.qiu.qoj.user.service;

import com.qiu.qoj.user.model.dto.file.UploadFileRequest;
import com.qiu.qoj.user.model.enums.FileUploadBizEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;


public interface FileService {
    void downloadFile(String filepath, HttpServletResponse response);

    String uploadFile(MultipartFile multipartFile, UploadFileRequest uploadFileRequest, HttpServletRequest request);

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum);

}
