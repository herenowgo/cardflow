package com.qiu.cardflow.api.controller;

import com.qiu.cardflow.api.common.BaseResponse;
import com.qiu.cardflow.api.service.IGroupService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@Slf4j
@RequiredArgsConstructor
public class GroupController {

    private final IGroupService groupService;

    @GetMapping
    @ApiOperation(value = "获取当前用户的所有分组")
    public BaseResponse<List<String>> getUserGroups() {
        return BaseResponse.success(groupService.getUserGroups());
    }

    @PostMapping("/{groupName}")
    @ApiOperation(value = "添加新分组")
    public BaseResponse<Boolean> addGroup(@PathVariable String groupName) {
        return BaseResponse.success(groupService.addGroup(groupName));
    }

    @PutMapping
    @ApiOperation(value = "更新分组列表")
    public BaseResponse<Boolean> updateGroups(@RequestBody List<String> groups) {
        return BaseResponse.success(groupService.updateGroups(groups));
    }
} 