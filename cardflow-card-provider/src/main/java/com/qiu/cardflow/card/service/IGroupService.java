package com.qiu.cardflow.card.service;

import com.qiu.cardflow.common.interfaces.exception.BusinessException;

import java.util.List;

public interface IGroupService {

    List<String> getUserGroups() throws BusinessException;

    Boolean addGroup(String groupName) throws BusinessException;

    Boolean updateGroups(List<String> groups) throws BusinessException;
}