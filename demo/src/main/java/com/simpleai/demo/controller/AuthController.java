package com.simpleai.demo.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.simpleai.demo.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 演示用认证接口。
 *
 * <p>真实项目里登录要校验密码、签发 JWT、写 Redis，
 * 这里只保留「登录 → 拿 token → 带 token 访问」这条主干。
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    /** 演示账号：admin（全权限）、guest（只读） */
    @PostMapping("/login")
    public Result<String> login(@RequestParam String username) {
        StpUtil.login(username);
        return Result.ok(StpUtil.getTokenValue());
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        StpUtil.checkLogin();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("loginId", StpUtil.getLoginId());
        data.put("permissions", StpUtil.getPermissionList());
        return Result.ok(data);
    }

    @PostMapping("/logout")
    public Result<String> logout() {
        StpUtil.logout();
        return Result.ok("已退出登录");
    }
}
