package com.example.springboot_shiro.service;


import com.example.springboot_shiro.entity.Perms;
import com.example.springboot_shiro.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;


public interface UserService {
    //   注册用户
    void register(User user);
    //根据用户名查询业务
    User findbByUserName(String username);

    //根据用户名查询所有角色
    User findRolesByUserName(String username);
    //根据角色id查询权限集合
    List<Perms> findPermsByRoleId(String id);

}
