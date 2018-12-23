package com.leyou.item.service;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecificationService {
    @Autowired
    private SpecGroupMapper specGroupMapper;
    @Autowired
    private SpecParamMapper specParamMapper;

    public List<SpecGroup> querySpecGroups(Long cid){
        SpecGroup t = new SpecGroup();
        t.setCid(cid);
        return specGroupMapper.select(t);
    }

    public List<SpecParam> querySpecParam(Long gid,Long cid,Boolean searching,Boolean generic) {
        SpecParam t = new SpecParam();
        t.setGroupId(gid);
        t.setCid(cid);
        t.setSearching(searching);
        t.setGeneric(generic);
        return specParamMapper.select(t);
    }

    public List<SpecGroup> querySpecsByCid(Long cid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        //根据分类id查询对应的规格参数表中的所有的规格组信息
        List<SpecGroup> select = specGroupMapper.select(specGroup);
        for (SpecGroup group : select) {
            //迭代每个规格组信息，查询组内对应的规格参数
            group.setParams(querySpecParam(group.getId(),null,null,null));
        }
        return select;
    }
}
