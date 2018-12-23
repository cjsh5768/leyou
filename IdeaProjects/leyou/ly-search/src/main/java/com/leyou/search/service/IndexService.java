package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.pojo.Goods;
import com.leyou.pojo.PageResult;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IndexService {

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private CategoryClient categoryClient;

    public Goods buildGoods(SpuBo spuBo){

            Goods goods = new Goods();
            BeanUtils.copyProperties(spuBo,goods);
            //以下数据没有直接数据，要封装

            //规格参数的查询根据分类以及是否可搜索来查询
            List<SpecParam> specParams = specificationClient.querySpecParam(null, spuBo.getCid3(), true, null);
            //根据spuId查询spu的详情信息
            SpuDetail spuDetail = goodsClient.querySpuDetailById(spuBo.getId());
            //获取所有的通用的规格参数
            Map<Long, String> genericMap = JsonUtils.parseMap(spuDetail.getGenericSpec(), Long.class, String.class);
            //获取所有特有的规格参数
            Map<Long,List<String>> specialMap = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
            });
            //处理规格参数显示问题，默认显示id+值，处理后显示名称+值
            HashMap<String,Object> specs = new HashMap<>();
            for (SpecParam param : specParams) {
                //规格参数的Id
                Long paramId = param.getId();
                //规格参数的名字，例如CPU品牌，电池容量
                String name = param.getName();
                //通用参数
                Object value = null;
                if (param.getGeneric()){
                    value = genericMap.get(paramId);
                    if (param.getNumeric()){
                        if (null==value){
                            value = "";
                        }
                        value = chooseSegment(value.toString(), param);
                    }
                }
                else {//特有参数
                    value = specialMap.get(paramId);
                }
                if (null == value){
                    value = "其他";
                }
                specs.put(name,value);
            }
            goods.setSpecs(specs);
            //当前spu对应的所有sku并且是个json
            List<Sku> skus = this.goodsClient.querySkuBySpuId(spuBo.getId());
            goods.setSkus(JsonUtils.serialize(skus));

            //当前spu对应的所有sku的price集合
            List<Long> priceList = new ArrayList<>();
            for (Sku sku : skus) {
                priceList.add(sku.getPrice());
            }
            goods.setPrice(priceList);

            //商品分类名称
            List<String> names = this.categoryClient.queryNameByIds(Arrays.asList(spuBo.getCid1(),spuBo.getCid2(),spuBo.getCid3()));
            String all = spuBo.getTitle()+" "+StringUtils.join(names," ");
            goods.setAll(all);
            return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public void createIndex(Long id){
        Spu spu = this.goodsClient.querySpuById(id);
        SpuBo spuBo = new SpuBo();
        BeanUtils.copyProperties(spu,spuBo);
        Goods goods = buildGoods(spuBo);
        //把goods对象保存到索引库
        this.goodsRepository.save(goods);
    }

    public void deleteIndex(Long id) {

    }
}
