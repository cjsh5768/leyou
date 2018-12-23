package com.leyou.item.controller;

import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import com.leyou.pojo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    @GetMapping("page")
    public ResponseEntity<PageResult<Brand>> queryBrandByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "desc", defaultValue = "false") Boolean desc,
            @RequestParam(value = "key", required = false) String key) {
            PageResult<Brand> result = this.brandService.queryBrandByPageAndSort(page,rows,sortBy,desc,key);
            if (result==null||result.getItems().size()==0){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<Void> saveBrand (Brand brand, @RequestParam ("cids")List<Long> cids){
        this.brandService.saveBrand(brand, cids);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandByCategory(@PathVariable("cid") Long cid){
        List<Brand> brandList = this.brandService.queryBrandByCategory(cid);
        if (brandList != null && 0!= brandList.size()) {
            return ResponseEntity.ok(brandList);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("list")
    public ResponseEntity<List<Brand>> queryBrandByIds(@RequestParam("ids") List<Long> ids){
        List<Brand> brandList = this.brandService.queryBrandByIds(ids);
        if (null!=brandList&&0!=brandList.size()){
            return ResponseEntity.ok(brandList);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
