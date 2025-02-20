package com.qiu.cardflow.card.interfaces;

import com.qiu.cardflow.card.model.dto.StudyResourceRequest;
import com.qiu.cardflow.card.model.dto.UpdateStudyResourceRequest;
import com.qiu.cardflow.card.model.dto.file.FilePreviewDTO;
import com.qiu.cardflow.card.model.vo.FileListVO;
import com.qiu.cardflow.card.model.vo.StudyResourceVO;
import com.qiu.cardflow.common.interfaces.exception.BusinessException;
import com.qiu.cardflow.common.interfaces.exception.RPC;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface IStudyResourceRPC extends RPC {

    String uploadFile(MultipartFile file, String path) throws Exception;

    void createFolder(String name, String parentPath) throws BusinessException;

    void delete(String id) throws BusinessException;

    Map<String, Long> getStorageStats() throws BusinessException;

    List<FileListVO> listFiles(String path) throws BusinessException;

    FilePreviewDTO getPreview(String id) throws Exception;

    void updateResource(UpdateStudyResourceRequest request) throws BusinessException;

    StudyResourceVO createResource(StudyResourceRequest request) throws BusinessException;

    StudyResourceVO getResourceById(String id) throws BusinessException;

    String uploadCover(MultipartFile file) throws Exception;
}