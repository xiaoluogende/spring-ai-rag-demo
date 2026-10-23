package com.simpleai.demo.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 拦截器：除登录接口和静态资源外，其余请求都要求已登录。
 *
 * <p>细粒度的权限校验用 {@code @SaCheckPermission} 注解标注在具体接口上，
 * 这样权限规则和业务代码放在一起，便于阅读。
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/error",
                        "/",
                        "/index.html",
                        "/assets/**",
                        "/favicon.svg",
                        "/icons.svg");
    }
}
