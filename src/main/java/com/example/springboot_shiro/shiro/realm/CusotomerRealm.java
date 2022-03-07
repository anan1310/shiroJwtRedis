package com.example.springboot_shiro.shiro.realm;

import com.example.springboot_shiro.entity.Perms;
import com.example.springboot_shiro.entity.User;

import com.example.springboot_shiro.service.UserService;
import com.example.springboot_shiro.shiro.salt.ByteSourceUtils;
import com.example.springboot_shiro.shiro.salt.MyByteSource;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.util.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.List;

//自定义realm
@Component
public class CusotomerRealm extends AuthorizingRealm {

//    @Autowired
//    private UserService userService;

    @Autowired
    private UserService userService;
//    授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
//1，获取身份信息
        String primaryPrincipal =(String) principalCollection.getPrimaryPrincipal();
        /*
        if ("anzhijie".equals(primaryPrincipal)){
            SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
//            添加角色
            simpleAuthorizationInfo.addRole("admin");
            simpleAuthorizationInfo.addRole("user");
//            赋予权限
            simpleAuthorizationInfo.addStringPermission("user:add:*");
            simpleAuthorizationInfo.addStringPermission("user:update:01");
            return  simpleAuthorizationInfo;

        }
         */

//2，根据身份信息获取角色和权限信息
        User user = userService.findRolesByUserName(primaryPrincipal);
        if (!CollectionUtils.isEmpty(user.getRoles())){
            SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
//            角色信息
            user.getRoles().forEach(role -> {
                simpleAuthorizationInfo.addRole(role.getName());
                //权限信息
                List<Perms> perms = userService.findPermsByRoleId(role.getId());
                System.out.println("perms:"+perms);
                if(!CollectionUtils.isEmpty(perms) && perms.get(0)!=null ){
                    perms.forEach(perm->{
                        simpleAuthorizationInfo.addStringPermission(perm.getName());
                    });
                }

            });
            return  simpleAuthorizationInfo;
        }
        return null;
    }

    //   认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
//       获取到身份信息
        String principal = (String) authenticationToken.getPrincipal();
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

/*
* 基于权限管理 ：role
*
* 基于资源管理 :permission
*
* */
