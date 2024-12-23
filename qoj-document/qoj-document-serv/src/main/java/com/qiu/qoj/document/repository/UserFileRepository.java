package com.qiu.qoj.document.repository;

import com.qiu.qoj.document.model.entity.UserFile;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

public interface UserFileRepository extends MongoRepository<UserFile, String> {

    List<UserFile> findByUserIdAndParentPathAndIsDeletedFalse(Long userId, String parentPath);

    UserFile findByPathAndIsDeletedFalse(String path);

    boolean existsByUserIdAndParentPathAndNameAndIsDeletedFalse(
            Long userId, String parentPath, String name);

    /**
     * 计算用户已使用的存储空间
     */
    @Aggregation(pipeline = {
        "{ $match: { 'userId': ?0, 'isDeleted': false, 'isFolder': false } }",
        "{ $group: { _id: null, total: { $sum: '$size' } } }",
        "{ $project: { _id: 0, total: 1 } }"
    })
    Long calculateUserStorageUsed(Long userId);

    /**
     * 统计指定文件夹下的文件数量
     */
    long countByUserIdAndParentPathAndIsDeletedFalse(Long userId, String parentPath);

    /**
     * 查找指定时间段内创建的文件
     */
    List<UserFile> findByUserIdAndCreateTimeBetweenAndIsDeletedFalse(
            Long userId, Date startTime, Date endTime);

    /**
     * 查找最近修改的文件
     */
    List<UserFile> findByUserIdAndIsDeletedFalseOrderByUpdateTimeDesc(
            Long userId, Pageable pageable);

    /**
     * 查找最近删除的文件
     */
    List<UserFile> findByUserIdAndIsDeletedTrueAndDeleteTimeGreaterThan(
            Long userId, Date time);
} 