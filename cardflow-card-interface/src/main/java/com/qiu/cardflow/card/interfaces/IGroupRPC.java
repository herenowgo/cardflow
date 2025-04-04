package com.qiu.cardflow.card.interfaces;

import com.qiu.cardflow.common.interfaces.exception.BusinessException;
import com.qiu.cardflow.common.interfaces.exception.RPC;

import java.util.List;

public interface IGroupRPC extends RPC {

    List<String> getUserGroups() throws BusinessException;

    Boolean addGroup(String groupName) throws BusinessException;

    Boolean updateGroups(List<String> groups) throws BusinessException;
}