package com.atwendu.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atwendu.gmall.bean.SkuLsInfo;
import com.atwendu.gmall.bean.SkuLsParams;
import com.atwendu.gmall.bean.SkuLsResult;
import com.atwendu.gmall.config.RedisUtil;
import com.atwendu.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    private JestClient jestClient;

    public static final String ES_INDEX="gmall";

    public static final String ES_TYPE="SkuInfo";

    @Autowired
    private RedisUtil redisUtil;


    @Override
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo) {
        Index index = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();

        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SkuLsResult search(SkuLsParams skuLsParams) {

        String query = makeQueryStringForSearch(skuLsParams);

        Search search = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();
        SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SkuLsResult skuLsResult = makeResultForSearch(searchResult, skuLsParams);

        return skuLsResult;
    }

    @Override
    public void incrHotScore(String skuId) {
        //获取Jedis
        Jedis jedis = redisUtil.getJedis();

        //定义key
        String hotKey = "hotScore";
        //保存数据 skuId:33, skuId:34
        Double count = jedis.zincrby(hotKey, 1, "skuId:" + skuId);
        //按照一定的规则更新es
        if(count%10 == 0) {
            //则更新一次es
            //es 更新语句
            updateHotScore(skuId, Math.round(count));
        }
    }

    /**
     * 更新
     * @param skuId
     * @param hotScore
     */
    private void updateHotScore(String skuId, long hotScore) {
        /*
        1.编写dsl语句
        2.定义动作
        3.执行
         */
        String upd = "{\n" +
                "  \"doc\": {\n" +
                "    \"hotScore\": "+hotScore+"\n" +
                "  }\n" +
                "}";
        Update update = new Update.Builder(upd).index(ES_INDEX).type(ES_TYPE).id(skuId).build();

        try {
            jestClient.execute(update);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //设置返回结果
    /**
     *
     * @param searchResult 通过dsl语句查询出来的结果
     * @param skuLsParams 入列参数
     * @return
     */
    private SkuLsResult makeResultForSearch(SearchResult searchResult, SkuLsParams skuLsParams) {

        //声明对象
        SkuLsResult skuLsResult = new SkuLsResult();

//        List<SkuLsInfo> skuLsInfoList;
        //声明一个集合来存储SkuLsInfo数据
        ArrayList<SkuLsInfo> skuLsInfoArrayList = new ArrayList<>();
        //给集合赋值
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        //循环遍历
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            SkuLsInfo skuLsInfo = hit.source;
            //获取skuName 的高亮
            if(hit.highlight != null && hit.highlight.size() > 0) {
                Map<String, List<String>> highlight = hit.highlight;
                List<String> list = highlight.get("skuName");
                //高亮的skuName
                String skuNameHI = list.get(0);//表示获取集合中的第一个数据
                skuLsInfo.setSkuName(skuNameHI);
            }
            skuLsInfoArrayList.add(skuLsInfo);
        }
        skuLsResult.setSkuLsInfoList(skuLsInfoArrayList);

//        long total;
        skuLsResult.setTotal(searchResult.getTotal());

//        long totalPages;
        long totalPages = searchResult.getTotal()%skuLsParams.getPageSize() == 0 ? searchResult.getTotal()/skuLsParams.getPageSize():searchResult.getTotal()/skuLsParams.getPageSize()+1;
//        long totalPages = (searchResult.getTotal() + skuLsParams.getPageSize() - 1)/skuLsParams.getPageSize();
        skuLsResult.setTotalPages(totalPages);
//        List<String> attrValueIdList;
        //声明一个集合来存储平台属性值Id
        ArrayList<String> stringArryList = new ArrayList<>();
        //获取平台属性值Id
        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation groupby_attr = aggregations.getTermsAggregation("groupby_attr");
        List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
        //循环遍历
        for (TermsAggregation.Entry bucket : buckets) {
            String valueId = bucket.getKey();
            stringArryList.add(valueId);
        }

        skuLsResult.setAttrValueIdList(stringArryList);
        return skuLsResult;
    }

    //完全根据手写的dsl语句
    private String makeQueryStringForSearch(SkuLsParams skuLsParams) {
        //定义一个查询器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //创建bool
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //判断keyword 是否为空
        if(skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length() > 0) {
            //创建match
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", skuLsParams.getKeyword());
            //创建must
            boolQueryBuilder.must(matchQueryBuilder);

            //设置高亮
            HighlightBuilder highlighter = searchSourceBuilder.highlighter();

            //设置高亮的规则
            highlighter.field("skuName");
            highlighter.preTags("<span style=color:red>");
            highlighter.postTags("</span>");

            //将设置好的高亮对象放入查询器中
            searchSourceBuilder.highlight(highlighter);

        }


        //判断平台属性值Id
        if(skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
            //循环
            for (String valueId : skuLsParams.getValueId()) {
                //创建term
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                //创建filter， 并添加term
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        //判断 三级分类Id
        if(skuLsParams.getCatalog3Id() != null && skuLsParams.getCatalog3Id().length() > 0){
            //创建term
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", skuLsParams.getCatalog3Id());
            //创建filter， 并添加term
            boolQueryBuilder.filter(termQueryBuilder);
        }

        //query ——bool
        searchSourceBuilder.query(boolQueryBuilder);

        //设置分页
        //from 从第几条开始查询
        //
        int from = (skuLsParams.getPageNo()-1)*skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        //size每页显示的条数
        searchSourceBuilder.size(skuLsParams.getPageSize());
        //设置排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        //聚合
        //创建一个对象 aggs:--terms
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr");
        //"field": "skuAttrValueList.valueId"
        groupby_attr.field("skuAttrValueList.valueId");
        //aggs放入查询器
        searchSourceBuilder.aggregation(groupby_attr);

        String query = searchSourceBuilder.toString();
        System.out.println("query:=" + query);
        return query;


    }
}
