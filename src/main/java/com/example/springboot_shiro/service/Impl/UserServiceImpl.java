package com.example.springboot_shiro.service.Impl;

import com.example.springboot_shiro.entity.Perms;
import com.example.springboot_shiro.entity.User;
import com.example.springboot_shiro.mapper.UserDAOMapper;
import com.example.springboot_shiro.service.UserService;
import com.example.springboot_shiro.utils.SaltUtils;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("userService")
@Transactional
public class UserServiceImpl implements UserService {
//业务层需要注入
    @Autowired
    private UserDAOMapper userDAOMapper;
    @Override
    public void register(User user) {
//        1.先判断当前用户已经存在
//       if (userDAOMapper.findbByUserName(user.getUsername()).equals(user.getUsername())) {
//        注册用户的时候需要对明文密码进行加盐
//        1.生成随机盐
            String salt = SaltUtils.getSalt(8);
//        将随机盐保存到数据库中
            user.setSalt(salt);
            Md5Hash md5Hash = new Md5Hash(user.getPassword(), salt, 1024);
            user.setPassword(md5Hash.toHex());
//        保存到数据库中
            userDAOMapper.save(user);
//        }
//
    }

    @Override
    public User findbByUserName(String username) {

        return userDAOMapper.findbByUserName(username);
    }
//根据用户名获取对应的角色
    @Override
    public User findRolesByUserName(String username) {
        return userDAOMapper.findRolesByUserName(username);
    }
//根据角色id获取对应的权限
    @Override
    public List<Perms> findPermsByRoleId(String id) {
        return userDAOMapper.findPermsByRoleId(id);
    }
}
