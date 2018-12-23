package com.leyou.auth.test;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {

    private static final String pubKeyPath = "/Users/ding/Documents/乐优公钥私钥/rsa.pub";

    private static final String priKeyPath = "/Users/ding/Documents/乐优公钥私钥/rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU0MzA3MjgxNX0.NmsD-Rwqz4-IQ2Gwe72hJK2Om4oizJu6_AoHJILhqCEJA0WeVj8cgLC_sOO1rtlKcSiEVKttkPgPQMZNCWeROUEk5MhrbfqOubPs11tQceCNhmGMC7x5D94URQLD5mNtiz6il3rfQnkeeRRxoL8TtzD6i9OByDROi4q7NmSGdZg";
        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}

