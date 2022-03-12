package com.example.springboot_shiro.controller;

import com.example.springboot_shiro.entity.User;

import com.example.springboot_shiro.service.UserService;
import com.example.springboot_shiro.utils.JedisUtil;
import com.example.springboot_shiro.utils.JwtUtils;
import com.example.springboot_shiro.utils.VerifyCodeUtils;
import com.example.springboot_shiro.utils.common.Constant;
import com.sun.deploy.net.HttpResponse;
import lombok.extern.log4j.Log4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

//@RestController("user")
//@Controller
//@RequestMapping("user")

@RestController
@RequestMapping("/user")
@PropertySource("classpath:config.properties")
public class UserController {
    /**
     * RefreshToken过期时间
     */
    @Value("${refreshTokenExpireTime}")
    private String refreshTokenExpireTime;
    @Autowired
    private UserService userService;

//    @Autowired
//    private UserTestService userService;

    //    验证码方法
    @RequestMapping("getImage")
    public void getImage(HttpSession session, HttpServletResponse response) {
//        生成验证码
        String code = VerifyCodeUtils.generateVerifyCode(4);
//        验证码放入session中
        session.setAttribute("code", code);
//        验证码存入图片
        try {
            ServletOutputStream os = response.getOutputStream();
//           设置响应的格式
            response.setContentType("image/png");
            VerifyCodeUtils.outputImage(220, 60, os, code);
        } catch (IOException e) {
            e.printStackTrace();
        }
//
    }

    //    登录页面
    @RequestMapping("login")
    public String login(String username, String password, String code, HttpSession session, HttpServletResponse httpServletResponse) {
//       获取到验证码进行比对
        String codes = (String) session.getAttribute("code");
        System.out.println("登录时候的验证码：" + codes);
        try {
            if (codes.equalsIgnoreCase(code)) { //比较不区分大小写
//            获取主体对象
                Subject subject = SecurityUtils.getSubject();
                UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(username, password);
                subject.login(usernamePasswordToken);


                // 设置RefreshToken，时间戳为当前时间戳，直接设置即可(不用先删后设，会覆盖已有的RefreshToken)
                String currentTimeMillis = String.valueOf(System.currentTimeMillis());  //获取当前时间
//                shiro:refresh_token:anzhijie 添加
                JedisUtil.setObject(Constant.PREFIX_SHIRO_REFRESH_TOKEN + Constant.ACCOUNT, currentTimeMillis, Integer.parseInt(refreshTokenExpireTime));
//              添加token到请求头中
                String token = JwtUtils.sign(usernamePasswordToken.getUsername(), currentTimeMillis);
                httpServletResponse.setHeader("Authorization", token);
                httpServletResponse.setHeader("Access-Control-Expose-Headers", "Authorization");

                return "redirect:/index.jsp";

            } else {
                throw new RuntimeException("验证码错误！");
            }
        } catch (UnknownAccountException e) {
            e.printStackTrace();
            System.out.println("用户名错误");
        } catch (IncorrectCredentialsException e) {
            e.printStackTrace();
            System.out.println("密码错误");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        return "redirect:/login.jsp";

    }

    // 退出登录
    @RequestMapping("logout")
    public String logout() {

        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return "redirect:/login.jsp";
    }

    //注册用户
    @RequestMapping("register")
    public String register(User user) {
        try {
            userService.register(user);
            return "redirect:/login.jsp";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/register.jsp";
        }


    }
}
