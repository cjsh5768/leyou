package com.leyou.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.pojo.PageResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;


@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> queryBrandByPageAndSort(Integer page, Integer rows, String sortBy, Boolean desc, String key){
        PageHelper.startPage(page,rows);
        Example example = new Example(Brand.class);
        if (StringUtils.isNotBlank(key)){
            Example.Criteria criteria = example.createCriteria();
            criteria.andLike("name","%"+key+"%").orEqualTo("letter",key.toUpperCase());
        }
        if (StringUtils.isNotBlank(sortBy)){
            String orderByClause = sortBy + (desc ? " DESC" : " ASC");
            example.setOrderByClause(orderByClause);
        }
        Page<Brand> pageInfo = (Page<Brand>) brandMapper.selectByExample(example);
        return new PageResult<>(pageInfo.getTotal(),pageInfo);
    }

    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        brandMapper.insertSelective(brand);
        for (Long cid : cids) {
            brandMapper.insertCategoryBrand(cid,brand.getId());
        }
    }

    public List<Brand> queryBrandByCategory(Long cid) {
        return this.brandMapper.queryBrandByCategory(cid);
    }

    public List<Brand> queryBrandByIds(List<Long> ids) {
        return this.brandMapper.selectByIdList(ids);
    }
}

