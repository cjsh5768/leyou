package com.leyou.page.service;

import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.pojo.Spu;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;

@Service
public class GoodsService {

    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private SpecificationClient specificationClient;

    public Map<String,Object> loadModel(Long id){
        Map<String,Object> modelMap = new HashMap<>();
        //封装spu
        Spu spu = goodsClient.querySpuById(id);
        modelMap.put("spu",spu);

        //封装skus，根据spuid查询对应的所有的sku
        modelMap.put("skus",goodsClient.querySkuBySpuId(id));

        //封装spuDetail
        modelMap.put("spuDetail",goodsClient.querySpuDetailById(id));

        modelMap.put("brand",brandClient.queryBrandByIds(Arrays.asList(spu.getBrandId())).get(0));

        List<Long> cids = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
        List<String> names = categoryClient.queryNameByIds(cids);
        List<Category> categories = new ArrayList<>();
        for (int i = 0; i < cids.size(); i++) {
            Category category = new Category();
            category.setId(cids.get(i));
            category.setName(names.get(i));
            categories.add(category);
        }

        modelMap.put("categories",categories);

        //查询规格组及组内参数,这个查询完成后只有属性没有值
        List<SpecGroup> groups = specificationClient.querySpecsByCid(spu.getCid3());
        modelMap.put("groups",groups);
        //查询商品分类下的特有规格参数
        List<SpecParam> params = specificationClient.querySpecParam(null,spu.getCid3(),null,false);
        //处理成id：name格式的键值对
        Map<Long,String> paramMap = new HashMap<>();
        for (SpecParam param : params) {
            paramMap.put(param.getId(),param.getName());
        }
        modelMap.put("paramMap",paramMap);
        return modelMap;
    }
}
