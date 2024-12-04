package com.qiu.qoj.user.feign;

import com.qiu.qoj.common.dto.BaseResponse;
import com.qiu.qoj.user.dto.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "qoj-user", path = "/api/user")
public interface UserService {

    @GetMapping("/get/vo")
     BaseResponse<UserVO> getUserVOById(@RequestParam Long id);


}
