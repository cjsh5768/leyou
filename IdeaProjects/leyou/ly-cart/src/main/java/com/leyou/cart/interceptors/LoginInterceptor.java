package com.leyou.cart.interceptors;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.cart.config.JwtProperties;
import com.leyou.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@EnableConfigurationProperties(JwtProperties.class)
@Component
public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private JwtProperties jwtProperties;

    //就是一个存储的空间，用来存储userInfo
    private static ThreadLocal<UserInfo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        if (null==token){
            response.setStatus(401);
            return false;
        }
        UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());

        //取值成功后我们需要把这个值共享，已备不时之需
        threadLocal.set(userInfo);

        return super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        threadLocal.remove();
    }

    public static UserInfo getUserInfo(){
        return threadLocal.get();
    }
}
