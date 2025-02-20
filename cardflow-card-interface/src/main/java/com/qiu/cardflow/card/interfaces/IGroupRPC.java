package com.qiu.cardflow.card.interfaces;

import com.qiu.cardflow.common.interfaces.exception.BusinessException;
import com.qiu.cardflow.common.interfaces.exception.RPC;

import java.util.List;

public interface IGroupRPC extends RPC {

    List<String> getUserGroups() throws BusinessException;

    boolean addGroup(String groupName) throws BusinessException;

    boolean updateGroups(List<String> groups) throws BusinessException;
}