package com.qiu.cardflow.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qiu.cardflow.api.common.BaseResponse;
import com.qiu.cardflow.api.service.IGroupService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/groups")
@Slf4j
@RequiredArgsConstructor
public class GroupController {

    private final IGroupService groupService;

    @GetMapping
    public BaseResponse<List<String>> getUserGroups() {
        return BaseResponse.success(groupService.getUserGroups());
    }

    @PostMapping("/{groupName}")
    public BaseResponse<Boolean> addGroup(@PathVariable String groupName) {
        return BaseResponse.success(groupService.addGroup(groupName));
    }

    @PutMapping
    public BaseResponse<Boolean> updateGroups(@RequestBody List<String> groups) {
        return BaseResponse.success(groupService.updateGroups(groups));
    }
}