package com.leyou.cart.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.cart.interceptors.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.websocket.server.ServerEndpoint;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {


    @Autowired
    private StringRedisTemplate redisTemplate;//Map<userId,Map<skuId,sku>>

    public void addCart(Cart cart) {
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //根据用户id找redis
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(userInfo.getId() + "");

        //在找到当前用户的所有购物车信息后，再在这当中找对应skuId的，如果找到了说明这个商品之前添加过。所以修改数量即可
        Object strCart = ops.get(cart.getSkuId() + "");

        //根本没有购物车
        if (null == strCart) {

            cart.setUserId(userInfo.getId());

            ops.put(cart.getSkuId() + "", JsonUtils.serialize(cart));
        } else {

            //找到了购物车对象，但是购物车当中没有当前sku

            Cart parse = JsonUtils.parse(strCart.toString(), Cart.class);

            parse.setNum(parse.getNum() + cart.getNum());

            ops.put(parse.getSkuId() + "", JsonUtils.serialize(parse));

        }
    }

    public List<Cart> queryCart(){

        UserInfo userInfo = LoginInterceptor.getUserInfo();

        String id = userInfo.getId()+"";

        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(id);

        List<Object> values = ops.values();

        if (null == values){

            return null;
        }

        List<Cart> carts = new ArrayList<>();

        for (Object value : values) {
            String strCart = value.toString();
            Cart cart = JsonUtils.parse(strCart,Cart.class);
            carts.add(cart);
        }
        return carts;
    }
}
