package com.qiu.cardflow.card.service.impl;


import com.qiu.cardflow.card.model.entity.Group;
import com.qiu.cardflow.card.repository.GroupRepository;
import com.qiu.cardflow.card.service.IGroupService;
import com.qiu.cardflow.common.interfaces.exception.Assert;
import com.qiu.cardflow.rpc.starter.RPCContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements IGroupService {

    private final GroupRepository groupRepository;

    // 获取用户的所有分组名称
    public List<String> getUserGroups() {
        Group group = groupRepository.findByUserId(RPCContext.getUserId());
        return group != null ? group.getName() : new ArrayList<>();
    }

    // 为用户添加新分组
    public Boolean addGroup(String groupName) {
        Group group = groupRepository.findByUserId(RPCContext.getUserId());
        if (group == null) {
            // 如果用户还没有分组文档，创建新的
            group = new Group();
            group.setUserId(RPCContext.getUserId());
            group.setName(new ArrayList<>());
        }

        // 检查分组名是否已存在
        Assert.isFalse(group.getName().contains(groupName), "分组名已存在");

        group.getName().add(groupName);
        groupRepository.save(group);
        return true;
    }

    // 更新用户的分组列表
    public Boolean updateGroups(List<String> groups) {
        Group group = groupRepository.findByUserId(RPCContext.getUserId());
        if (group == null) {
            group = new Group();
            group.setUserId(RPCContext.getUserId());
        }

        group.setName(groups);
        groupRepository.save(group);
        return true;
    }
} 