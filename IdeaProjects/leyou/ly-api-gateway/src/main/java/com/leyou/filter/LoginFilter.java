package com.leyou.filter;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.config.FilterProperties;
import com.leyou.config.JwtProperties;
import com.leyou.utils.CookieUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
@EnableConfigurationProperties({JwtProperties.class,FilterProperties.class})
public class LoginFilter extends ZuulFilter {

    @Autowired
    private JwtProperties prop;
    
    @Autowired
    private FilterProperties filterProperties;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {

        //获取当前请求的上下文
        RequestContext currentContext = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = currentContext.getRequest();

        String requestURI = request.getRequestURI();
        //check

        return checkIsFilter(requestURI);
    }

    public Boolean checkIsFilter(String requestURI){
        List<String> allowPaths = filterProperties.getAllowPaths();

        Boolean isFilter = true;

        for (String path : allowPaths) {
            if (requestURI.startsWith(path)){
                isFilter = false;
                break;
            }
        }
        return isFilter;
    }

    @Override
    public Object run() throws ZuulException {
        //获取当前请求的上下文
        RequestContext currentContext = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = currentContext.getRequest();
        // 获取token
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        //使用jwt工具解析token，如果能解析，那么放行，如果解析失败，首先停止响应，其次设置响应状态码
        try {
            JwtUtils.getInfoFromToken(token, prop.getPublicKey());
        } catch (Exception e) {
            currentContext.setSendZuulResponse(false);
            currentContext.setResponseStatusCode(401);
        }
        return null;
    }
}
