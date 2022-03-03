package com.example.springboot_shiro.mapper;

import com.example.springboot_shiro.entity.Perms;
import com.example.springboot_shiro.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

//@Mapper
@Repository
public interface UserDAOMapper {

    void save(User user);
    User findbByUserName(String username);
    //根据用户名查询所有角色
    User findRolesByUserName(String username);
    //根据角色id查询权限集合
    List<Perms> findPermsByRoleId(String id);
}
