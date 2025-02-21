package com.qiu.cardflow.api.service.impl;

import com.qiu.cardflow.api.service.IGroupService;
import com.qiu.cardflow.card.interfaces.IGroupRPC;
import com.qiu.cardflow.common.interfaces.exception.BusinessException;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IGroupServiceImpl implements IGroupService {
    @DubboReference
    IGroupRPC groupRPC;

    @Override
    public List<String> getUserGroups() throws BusinessException {
        return groupRPC.getUserGroups();
    }

    @Override
    public Boolean addGroup(String groupName) throws BusinessException {
        return groupRPC.addGroup(groupName);
    }

    @Override
    public Boolean updateGroups(List<String> groups) throws BusinessException {
        return groupRPC.updateGroups(groups);
    }
}
