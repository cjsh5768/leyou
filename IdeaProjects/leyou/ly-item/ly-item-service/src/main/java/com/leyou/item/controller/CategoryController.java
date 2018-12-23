package com.leyou.item.controller;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("category")
public class CategoryController {


    @Autowired
    private CategoryService categoryService;

    @GetMapping("list")
    public ResponseEntity<List<Category>> list(@RequestParam(value = "pid",defaultValue = "0") Long pid){
        List<Category> categoryList = categoryService.queryListByParent(pid);
        if (null!=categoryList&&0!=categoryList.size()){
            return ResponseEntity.ok(categoryList);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryByBrandId(@PathVariable("bid") Long bid){
        List<Category> list = categoryService.queryByBrandId(bid);
        if (list == null || list.size()<1){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(list);
    }

    /**
     * 根据商品分类id查询名称
     * @param ids 要查询的分类id集合
     * @return 多个名称的集合
     */
    @GetMapping("names")
    public ResponseEntity<List<String>> queryNameByIds(@RequestParam("ids") List<Long> ids){
        List<String> nameList = categoryService.queryNamesByIds(ids);
        if (null!=nameList&&0!=nameList.size()){
            return ResponseEntity.ok(nameList);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
