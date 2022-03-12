package com.example.springboot_shiro.shiro.jwt;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.springboot_shiro.entity.common.ResponseBean;
import com.example.springboot_shiro.exception.CustomException;
import com.example.springboot_shiro.utils.JedisUtil;
import com.example.springboot_shiro.utils.JwtUtils;
import com.example.springboot_shiro.utils.common.Constant;
import com.example.springboot_shiro.utils.common.JsonConvertUtil;
import com.example.springboot_shiro.utils.common.PropertiesUtil;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

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
        //查看当前header中是否携带Authorization属性中的（Token）
        if (this.isLoginAttempt(request, response)) {
            try {
                //登录shiroUserRealm
                this.executeLogin(request, response);
            } catch (Exception e) {
                // 认证出现异常，传递错误信息msg
                String msg = e.getMessage();
                // 获取应用异常(该Cause是导致抛出此throwable(异常)的throwable(异常))
                Throwable throwable = e.getCause();
                if (throwable instanceof SignatureVerificationException) {
                    // 该异常为JWT的AccessToken认证失败(Token或者密钥不正确)
                    msg = "Token或者密钥不正确(" + throwable.getMessage() + ")";
                } else if (throwable instanceof TokenExpiredException) {
                    // 该异常为JWT的AccessToken已过期，判断RefreshToken未过期就进行AccessToken刷新
                   if (this.refreshToken(request, response)) {
                        return true;
                    } else {
                        msg = "Token已过期(" + throwable.getMessage() + ")";
                    }

                    msg = "Token已过期(" + throwable.getMessage() + ")";
                } else {
                    // 应用异常不为空
                    if (throwable != null) {
                        // 获取应用异常msg
                        msg = throwable.getMessage();
                    }
                }
                // Token认证失败直接返回Response信息
                this.response401(response, msg);
                return false;
            }
        } else {

            // 没有携带Token
            HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
            // 获取当前请求类型
            String httpMethod = httpServletRequest.getMethod();
            // 获取当前请求URI
            String requestURI = httpServletRequest.getRequestURI();
            logger.info("当前请求 {} Authorization属性(Token)为空 请求类型 {}", requestURI, httpMethod);
            // mustLoginFlag = true 开启任何请求必须登录才可访问
            final Boolean mustLoginFlag = false;
            if (mustLoginFlag) {
                this.response401(response, "请先登录");
                return false;
            }
        }

        return true;

    }

    /**
     * 进行AccessToken登录认证授权
     */
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        // 拿到当前Header中Authorization的AccessToken(Shiro中getAuthzHeader方法已经实现)
        JwtToken token = new JwtToken(this.getAuthzHeader(request));
        // 提交给UserRealm进行认证，如果错误他会抛出异常并被捕获
        this.getSubject(request, response).login(token);
        // 如果没有抛出异常则代表登入成功，返回true
        return true;
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
        return token != null;

    }

    /**
     * 无需转发，直接返回Response信息
     */
    private void response401(ServletResponse response, String msg) {
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
        httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("application/json; charset=utf-8");
        try (PrintWriter out = httpServletResponse.getWriter()) {
            String data = JsonConvertUtil.objectToJson(new ResponseBean(HttpStatus.UNAUTHORIZED.value(), "无权访问(Unauthorized):" + msg, null));
            out.append(data);
        } catch (IOException e) {
            logger.error("直接返回Response信息出现IOException异常:{}", e.getMessage());
            throw new CustomException("直接返回Response信息出现IOException异常:" + e.getMessage());
        }
    }

    /*
          此处为AccessToken刷新，进行判断refreshToken是否过期，未过期使用返回新的AccessToken进行登录
     */
    private boolean refreshToken(ServletRequest request, ServletResponse response) {
//        1.从请求中获取该字段,获取到token
        String token = this.getAuthzHeader(request);
//        2.获取到当前token的账号信息（用户名）
        String account = JwtUtils.getClaim(token, Constant.ACCOUNT);
//        3.判断redis中的refreshToken是否存在
        if (JedisUtil.exists(Constant.PREFIX_SHIRO_REFRESH_TOKEN + account)) {//shiro:refresh_token:account
//           3.1 如果redis中的refreshToken已经存在，获取到redis中的的时间戳
            String currentTimeMillisRedis = JedisUtil.getObject(Constant.PREFIX_SHIRO_REFRESH_TOKEN + account).toString();
//           4 获取到AccessToken中的时间戳与redis中的refresh_token中的时间戳进行比对，如果当前时间一致的话，进行AccessToken刷新
            if (JwtUtils.getClaim(token, Constant.CURRENT_TIME_MILLIS).equals(currentTimeMillisRedis)) {
//           4.1 获取当前时间戳
                String currentTimeMillis = String.valueOf(System.currentTimeMillis());
//           4.2 读取配置文件，获取refresh_token 的属性（过期时间等）
                PropertiesUtil.readProperties("config.properties");
                String refreshTokenExpireTime = PropertiesUtil.getProperty("refreshTokenExpireTime");
//          4.3设置key 为refresh_token value为最新的时间戳，并且指定过期时间为30分钟（配置文件中进行设置）
                JedisUtil.setObject(Constant.PREFIX_SHIRO_REFRESH_TOKEN + account, currentTimeMillis, Integer.parseInt(refreshTokenExpireTime));

//          4.4  刷新AccessToken （生成新的token值,将之前token进行重赋值）
                token = JwtUtils.sign(Constant.ACCOUNT, currentTimeMillis);
                // 将新刷新的AccessToken再次进行Shiro的登录
                JwtToken jwtToken = new JwtToken(token);
//          4.5 提交给realm进行认证，如果认证成功，就进行登录
                this.getSubject(request, response).login(jwtToken);
//          5.最后将Authorization字段返回
                HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
                httpServletRequest.setAttribute("Authorization", token);
//                如果想要让客户端可以访问到其他的首部信息,可以将对应的值暴露给外部访问
                httpServletRequest.setAttribute("Access-Control-Expose-Headers", "Authorization");
                return true;


            }


        }

return  false;

    }

}
