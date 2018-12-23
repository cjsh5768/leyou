package com.leyou.cart.controller;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart){
        cartService.addCart(cart);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Cart>> queryCart(){
        List<Cart> carts = this.cartService.queryCart();
        if (null == carts){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(carts);
    }
}
