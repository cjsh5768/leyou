package com.leyou.search.service;

import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.SpecParam;
import com.leyou.pojo.Goods;
import com.leyou.pojo.SearchRequest;
import com.leyou.pojo.SearchResult;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private SpecificationClient specificationClient;

    public SearchResult search(SearchRequest searchRequest){
        String key = searchRequest.getKey();
        if (StringUtils.isBlank(key)){
            return null;
        }
        //查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //过滤筛选本次查询要哪些结果
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","skus","subTitle"},null));

        QueryBuilder query = buildBasicQueryWithFilter(searchRequest);
        //查询
        queryBuilder.withQuery(query);
        //分页
        queryBuilder.withPageable(PageRequest.of(searchRequest.getPage()-1,searchRequest.getSize()));
        //聚合
        String categoryAggName = "category"; //商品分类聚合名称
        String brandAggName = "brand";  //品牌聚合名称
        //根据分类id聚合
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        //根据品牌id聚合
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        //执行查询
        AggregatedPage<Goods> aggregatedPage = (AggregatedPage<Goods>) goodsRepository.search(queryBuilder.build());
        //从查询结果中获取分类聚合信息
        LongTerms categoryTerms = (LongTerms) aggregatedPage.getAggregation(categoryAggName);
        //获取分类中的桶
        List<LongTerms.Bucket> buckets = categoryTerms.getBuckets();
        //存储每个分类的cid3
        List<Long> cids = new ArrayList<>();
        for (LongTerms.Bucket bucket : buckets) {
            cids.add(bucket.getKeyAsNumber().longValue());
        }
        //根据cid3查询cid3所对应的分类的名字，例如76对应手机
        List<String> names = categoryClient.queryNameByIds(cids);
        //保存具体的分类的对象
        List<Category> categories = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            Category category = new Category();
            category.setId(cids.get(i));
            category.setName(names.get(i));
            categories.add(category);
        }
        //从查询结果中获取品牌聚合信息
        LongTerms brandTerms = (LongTerms) aggregatedPage.getAggregation(brandAggName);
        List<LongTerms.Bucket> brandBuckets = brandTerms.getBuckets();
        List<Long> brandIds = new ArrayList<>();
        for (LongTerms.Bucket bucket : brandBuckets) {
            brandIds.add(bucket.getKeyAsNumber().longValue());
        }
        List<Brand> brandList = brandClient.queryBrandByIds(brandIds);

        List<Map<String,Object>> specs = null;
        //判断规格参数是否聚合
        if (categories.size()==1){
            specs = getSpecs(categories.get(0).getId(),query);
        }
        return new SearchResult(aggregatedPage.getTotalElements(),new Long(aggregatedPage.getTotalPages()),aggregatedPage.getContent(),categories,brandList,specs);
    }

      //根据分类id进行规格参数查询，并且对可以过滤的字段进行聚合查询
    private List<Map<String,Object>> getSpecs(Long cid,QueryBuilder query){
        //根据分类的信息查询当前分类可以搜索的规格参数
        List<SpecParam> specParams = specificationClient.querySpecParam(null, cid, true, null);
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //聚合时候要根据查询结果来
        queryBuilder.withQuery(query);

        for (SpecParam specParam : specParams) {
            String name = specParam.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs."+name+".keyword"));
        }
        //查询,返回商品信息
        Map<String, Aggregation> aggs = ((AggregatedPage<Goods>) this.goodsRepository.search(queryBuilder.build())).getAggregations().asMap();
        List<Map<String,Object>> specs = new ArrayList<>();
        for (SpecParam specParam : specParams) {
            Map<String,Object> spec = new HashMap<>();
            String name = specParam.getName();
            spec.put("k",name);

            StringTerms stringTerms = (StringTerms) aggs.get(name);
            List<String> opts = new ArrayList<>();
            for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
                opts.add(bucket.getKeyAsString());
            }
            spec.put("options",opts);
            specs.add(spec);
        }
        return specs;
    }
    // 构建基本查询条件
    private QueryBuilder buildBasicQueryWithFilter(SearchRequest request) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 基本查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND));
        // 过滤条件构建器
        BoolQueryBuilder filterQueryBuilder = QueryBuilders.boolQuery();
        // 整理过滤条件
        Map<String, String> filter = request.getFilter();
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            // 商品分类和品牌已经聚合
            if (key != "cid3" && key != "brandId") {
                key = "specs." + key + ".keyword";
            }
            // 字符串类型，进行term查询
            filterQueryBuilder.must(QueryBuilders.termQuery(key, value));
        }
        // 添加过滤条件
        queryBuilder.filter(filterQueryBuilder);
        return queryBuilder;
    }
}
