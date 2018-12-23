package com.leyou.item.bo;

import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;

import javax.persistence.Transient;
import java.util.List;

public class SpuBo extends Spu {

    @Transient
    private String cname;// 商品分类名称

    @Transient
    private String bname;// 品牌名称

    @Transient
    private SpuDetail spuDetail;

    @Transient
    private List<Sku> skus;
    // getter ,setter

    public String getCname() {
        return cname;
    }

    public SpuDetail getSpuDetail() {
        return spuDetail;
    }

    public void setSpuDetail(SpuDetail spuDetail) {
        this.spuDetail = spuDetail;
    }

    public List<Sku> getSkus() {
        return skus;
    }

    public void setSkus(List<Sku> skus) {
        this.skus = skus;
    }

    public void setCname(String cname) {
        this.cname = cname;

    }

    public String getBname() {
        return bname;
    }

    public void setBname(String bname) {
        this.bname = bname;
    }
}