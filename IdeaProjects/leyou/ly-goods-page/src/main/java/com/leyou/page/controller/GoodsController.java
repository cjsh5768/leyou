package com.leyou.page.controller;

import com.leyou.page.service.FileService;
import com.leyou.page.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("item")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private FileService fileService;
    /**
     * 跳转到商品详情页
     * @param model
     * @param id
     * @return
     */
    @GetMapping("{id}.html")
    public String toItemPage(Model model, @PathVariable("id")Long id){
       model.addAllAttributes(goodsService.loadModel(id));
        if(!this.fileService.exists(id)){
            this.fileService.syncCreateHtml(id);
        }
        return "item";
    }
}