package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.user.pojo.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserClient userClient;

    public String authentication(String username, String password,JwtProperties jwtProperties) {
        User user = userClient.queryUser(username, password);
        if (null !=user){
            //生成token
            UserInfo userInfo = new UserInfo();
            BeanUtils.copyProperties(user,userInfo);
            try {
                String token = JwtUtils.generateToken(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
                return token;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
