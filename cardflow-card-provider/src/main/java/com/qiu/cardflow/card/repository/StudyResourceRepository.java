package com.qiu.cardflow.card.repository;

import com.qiu.cardflow.card.model.entity.StudyResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;

public interface StudyResourceRepository extends MongoRepository<StudyResource, String> {

    /**
     * 查询指定目录下的文件和文件夹的基本信息（只返回ID、名称、类型和资源类型）
     */
    @Query(value = "{'userId': ?0, 'parentPath': ?1, 'isDeleted': false}", fields = "{'id': 1, 'name': 1, 'isFolder': 1, 'resourceType': 1}")
    List<StudyResource> findBasicFileInfoByUserIdAndParentPath(Long userId, String parentPath);

    List<StudyResource> findByUserIdAndParentPathAndIsDeletedFalse(Long userId, String parentPath);

    /**
     * 根据父目录路径和名称查找资源
     */
    StudyResource findByParentPathAndNameAndIsDeletedFalse(String parentPath, String name);

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
    List<StudyResource> findByUserIdAndCreateTimeBetweenAndIsDeletedFalse(
            Long userId, Date startTime, Date endTime);

    /**
     * 查找最近修改的文件
     */
    List<StudyResource> findByUserIdAndIsDeletedFalseOrderByUpdateTimeDesc(
            Long userId, Pageable pageable);

    /**
     * 查找最近删除的文件
     */
    List<StudyResource> findByUserIdAndIsDeletedTrueAndDeleteTimeGreaterThan(
            Long userId, Date time);
}