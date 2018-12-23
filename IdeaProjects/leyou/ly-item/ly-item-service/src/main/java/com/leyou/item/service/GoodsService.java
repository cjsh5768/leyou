package com.leyou.item.service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import com.leyou.pojo.PageResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    Logger logger = LoggerFactory.getLogger(GoodsService.class);

    public PageResult<SpuBo> querySpuByPageAndSort(Integer page, Integer rows, Boolean saleable, String key) {
        // 1、查询SPU
        // 分页,最多允许查100条
        PageHelper.startPage(page, Math.min(rows,200));

        // 创建查询条件
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        // 是否过滤上下架
        if (saleable != null) {
            criteria.orEqualTo("saleable",saleable);
        }

        // 是否模糊查询
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        Page<Spu> pageInfo = (Page<Spu>) this.spuMapper.selectByExample(example);

        List<SpuBo> list = pageInfo.getResult().stream().map(spu -> {
            // 把spu变为 spuBo
            SpuBo spuBo = new SpuBo();
            // 属性拷贝
            BeanUtils.copyProperties(spu,spuBo);

            // 2、查询spu的商品分类名称,要查三级分类
            List<String> names = this.categoryService.queryNamesByIds(
                    Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            // 将分类名称拼接后存入
            spuBo.setCname(StringUtils.join(names, "/"));

            // 3、查询spu的品牌名称
            Brand brand = this.brandMapper.selectByPrimaryKey(spu.getBrandId());
            spuBo.setBname(brand.getName());
            return spuBo;
        }).collect(Collectors.toList());

        return new PageResult<>(pageInfo.getTotal(),list);
    }

    @Transactional
    public void save(SpuBo spu) {
        spu.setSaleable(true);
        spu.setCreateTime(new Date());
        spu.setValid(true);
        spu.setLastUpdateTime(spu.getLastUpdateTime());
        //插入spu
        spuMapper.insert(spu);
        spu.getSpuDetail().setSpuId(spu.getId());
        //插入spuDetail
        spuDetailMapper.insert(spu.getSpuDetail());
        saveSkuAndStock(spu.getSkus(),spu.getId());

        sendMessage(spu.getId(),"insert");
    }

    private void saveSkuAndStock(List<Sku> skus, Long spuId) {
        for (Sku sku : skus) {
            if (!sku.getEnable()){
                continue;
            }
            sku.setSpuId(spuId);
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insert(sku);
            //保存库存信息
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insert(stock);
        }
    }

    public SpuDetail querySpuDetailById(Long id) {
       return spuDetailMapper.selectByPrimaryKey(id);
    }

    public List<Sku> querySkuBySpuId(Long id) {
        Sku s = new Sku();
        s.setSpuId(id);
        List<Sku> skus = skuMapper.select(s);
        for (Sku sku : skus) {
            sku.setStock(stockMapper.selectByPrimaryKey(sku.getId()).getStock());
        }
        return skus;
    }

    @Transactional
    public void update(SpuBo spu) {
        List<Sku> skus = querySkuBySpuId(spu.getId());
        if (!CollectionUtils.isEmpty(skus)){
            List<Long> ids = skus.stream().map(sku -> sku.getId()).collect(Collectors.toList());
            Example example = new Example(Stock.class);
            example.createCriteria().andIn("skuId",ids);
            this.stockMapper.deleteByExample(example);
            //删除以前的sku
            Sku record = new Sku();
            record.setSpuId(spu.getId());
            this.skuMapper.delete(record);
        }
        //新增sku和库存
        saveSkuAndStock(skus,spu.getId());
        //更新spu
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);
        spu.setValid(null);
        spu.setSaleable(null);
        this.spuMapper.updateByPrimaryKeySelective(spu);
        //更新spu详情
        this.spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());

        sendMessage(spu.getId(),"update");
    }

    public Spu querySpuById(Long id) {
       return spuMapper.selectByPrimaryKey(id);
    }

    private void sendMessage(Long id, String type){
        // 发送消息
        try {
            this.amqpTemplate.convertAndSend("item." + type, id);
        } catch (Exception e) {
            logger.error("{}商品消息发送异常，商品id：{}", type, id, e);
        }
    }
}