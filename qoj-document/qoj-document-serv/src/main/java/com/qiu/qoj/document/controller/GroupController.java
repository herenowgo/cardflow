package com.qiu.qoj.document.controller;

import com.qiu.qoj.common.api.BaseResponse;
import com.qiu.qoj.common.api.UserContext;
import com.qiu.qoj.document.service.impl.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
@Slf4j
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    // 获取当前用户的所有分组
    @GetMapping
    public BaseResponse<List<String>> getUserGroups() {
        return BaseResponse.success(groupService.getUserGroups(UserContext.getUserId()));
    }

    // 添加新分组
    @PostMapping("/{groupName}")
    public BaseResponse<Boolean> addGroup(@PathVariable String groupName) {
        return BaseResponse.success(groupService.addGroup(UserContext.getUserId(), groupName));
    }

    // 更新分组列表
    @PutMapping
    public BaseResponse<Boolean> updateGroups(@RequestBody List<String> groups) {
        return BaseResponse.success(groupService.updateGroups(UserContext.getUserId(), groups));
    }
} 