package com.example.springboot_shiro.shiro.jwt;

import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class JwtFilter extends BasicHttpAuthenticationFilter {
    /**
     * logger ：在日志可以进行打印详细的日志信息 Log.info(“要打印的内容”)
     */
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);


    /*
     * 是否允许登录
     * */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {



        return false;

    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        this.sendChallenge(request, response);
        return false;
    }

    /*
     检查header中是否含有Authorization字段，有进行token登录授权,没有获取到就返回false
     getAuthzHeader 方法shiro已经实现
     */
    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        String token = this.getAuthzHeader(request);
        return token!=null;

    }

}
