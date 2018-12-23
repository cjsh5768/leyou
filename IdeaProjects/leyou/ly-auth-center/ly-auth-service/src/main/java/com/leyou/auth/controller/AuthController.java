package com.leyou.auth.controller;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("accredit")
    public ResponseEntity<Void> authentication(@RequestParam("username") String username,
                                               @RequestParam("password") String password,
                                               HttpServletRequest request,
                                               HttpServletResponse response){
        // 登录校验
        String token = this.authService.authentication(username, password,jwtProperties);
        //拿到token后把token存入cookie
        CookieUtils.setCookie(request,response,jwtProperties.getCookieName(),token,jwtProperties.getCookieMaxAge());
        return ResponseEntity.ok().build();
    }
    /**
     * 验证用户信息
     *
     * @param token
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verifyUser(@CookieValue("LY_TOKEN") String token,
                                               HttpServletRequest request, HttpServletResponse response) {
        try {
            // 获取token信息
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            // 如果成功，我们还需要刷新token
            String newToken = JwtUtils.generateToken(userInfo,
                    jwtProperties.getPrivateKey(), jwtProperties.getExpire());
            // 然后写入cookie中
            // 将token写入cookie,并指定httpOnly为true，防止通过JS获取和修改
            CookieUtils.setCookie(request, response, jwtProperties.getCookieName(),
                    newToken, jwtProperties.getCookieMaxAge(), null, true);
            // 成功后直接返回
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            // 抛出异常，证明token无效，直接返回401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
}
