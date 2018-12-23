package com.leyou.user.service;

import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.utils.CodecUtils;
import com.leyou.utils.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;

    Logger logger = LoggerFactory.getLogger(UserService.class);

    public Boolean checkUserData(String data, Integer type) {
        switch (type){
            case 1:
                User user = new User();
                user.setUsername(data);
                return this.userMapper.selectCount(user)==0;
            case 2:
                User user1 = new User();
                user1.setPhone(data);
                return this.userMapper.selectCount(user1)==0;
        }
        return null;
    }

    static final String KEY_PREFIX = "user:code:phone:";

    public Boolean sendVerifyCode(String phone) {
        try {
            //生成验证码
            String verifyCode = NumberUtils.generateCode(7);
            //可以开始发送消息了
            Map<String,String> msg = new HashMap<>();
            msg.put("phone",phone);
            msg.put("code",verifyCode);
            amqpTemplate.convertAndSend("ly.sms.exchange","sms.verify.code",msg);
            //没有报错，就应该把发送的验证码保存到redis中
            redisTemplate.opsForValue().set(KEY_PREFIX+phone,verifyCode,5,TimeUnit.MINUTES);
        } catch (AmqpException e) {
            e.printStackTrace();
        }
        return true;
    }

    public Boolean register(User user, String code) {
        String key = KEY_PREFIX + user.getPhone();
        //第一步，看验证码输入是否正确
        String redisCode = redisTemplate.opsForValue().get(key);
        if (!code.equals(redisCode)){
            return false;
        }
        //验证码输入正确，进入保存流程
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));
        user.setCreated(new Date());
        // 写入数据库
        boolean boo = this.userMapper.insertSelective(user) == 1;
        // 如果注册成功，删除redis中的code
        if (boo) {
            try {
                this.redisTemplate.delete(key);
            } catch (Exception e) {
                logger.error("删除缓存验证码失败，code：{}", code, e);
            }
        }
        return boo;
    }

    public User queryUser(String username, String password) {
        User record = new User();
        record.setUsername(username);
        //根据用户名查询一个对象
        User user = this.userMapper.selectOne(record);
        if (null!=user){
            String salt = user.getSalt();
            String pass = CodecUtils.md5Hex(password,salt);
            if (user.getPassword().equals(pass)){
                return user;
            }
        }
        return null;
    }
}
