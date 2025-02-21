package com.qiu.cardflow.card.rpc;

import com.qiu.cardflow.card.interfaces.IGroupRPC;
import com.qiu.cardflow.card.service.IGroupService;
import com.qiu.cardflow.common.interfaces.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@DubboService
@Slf4j
@RequiredArgsConstructor
@Validated
public class GroupRPCImpl implements IGroupRPC {

    private final IGroupService groupService;

    @Override
    public List<String> getUserGroups() throws BusinessException {
        return groupService.getUserGroups();
    }

    @Override
    public Boolean addGroup(String groupName) throws BusinessException {
        return groupService.addGroup(groupName);
    }

    @Override
    public Boolean updateGroups(List<String> groups) throws BusinessException {
        return groupService.updateGroups(groups);
    }
}
