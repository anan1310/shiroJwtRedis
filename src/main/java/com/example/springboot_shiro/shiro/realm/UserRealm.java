package com.example.springboot_shiro.shiro.realm;

import com.example.springboot_shiro.entity.User;
import com.example.springboot_shiro.service.UserService;
import com.example.springboot_shiro.shiro.salt.ByteSourceUtils;
import com.example.springboot_shiro.utils.JedisUtil;
import com.example.springboot_shiro.utils.JwtUtils;
import com.example.springboot_shiro.utils.common.Constant;
import com.example.springboot_shiro.utils.common.StringUtil;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

//自定义realm
@Component
public class UserRealm extends AuthorizingRealm {
    @Autowired
    private UserService userService;

    //授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return null;
    }

    //认证 使用此方法用来验证用户名和密码是否正确
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
//       获取到身份信息
        String principal = (String) authenticationToken.getPrincipal();
        System.out.println(principal);
//        通过表单提交的信息查询到对应的用户
        User user = userService.findbByUserName(principal);
        if (!ObjectUtils.isEmpty(user)) {
            // 参数一：身份信息 参数二：凭证信息 参数三：获取到盐值 参数四：当前realm的名称
            SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(
                    user.getUsername(),
                    user.getPassword(),
//                            ByteSource.Util.bytes(user.getSalt()),
                    ByteSourceUtils.bytes(user.getSalt()),//salt
//                    new MyByteSource(user.getSalt()),
                    this.getName());
            return simpleAuthenticationInfo;
        }

//        if ("anzhijie".equals(principal)) {
//            System.out.println(this.getName());//当前realm的名称
//            return new SimpleAuthenticationInfo(principal, "123", this.getName());
//        }
        return null;
    }
}