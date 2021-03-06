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
        //??????Jedis
        Jedis jedis = redisUtil.getJedis();

        //??????key
        String hotKey = "hotScore";
        //???????????? skuId:33, skuId:34
        Double count = jedis.zincrby(hotKey, 1, "skuId:" + skuId);
        //???????????????????????????es
        if(count%10 == 0) {
            //???????????????es
            //es ????????????
            updateHotScore(skuId, Math.round(count));
        }
    }

    /**
     * ??????
     * @param skuId
     * @param hotScore
     */
    private void updateHotScore(String skuId, long hotScore) {
        /*
        1.??????dsl??????
        2.????????????
        3.??????
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

    //??????????????????
    /**
     *
     * @param searchResult ??????dsl???????????????????????????
     * @param skuLsParams ????????????
     * @return
     */
    private SkuLsResult makeResultForSearch(SearchResult searchResult, SkuLsParams skuLsParams) {

        //????????????
        SkuLsResult skuLsResult = new SkuLsResult();

//        List<SkuLsInfo> skuLsInfoList;
        //???????????????????????????SkuLsInfo??????
        ArrayList<SkuLsInfo> skuLsInfoArrayList = new ArrayList<>();
        //???????????????
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        //????????????
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            SkuLsInfo skuLsInfo = hit.source;
            //??????skuName ?????????
            if(hit.highlight != null && hit.highlight.size() > 0) {
                Map<String, List<String>> highlight = hit.highlight;
                List<String> list = highlight.get("skuName");
                //?????????skuName
                String skuNameHI = list.get(0);//???????????????????????????????????????
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
        //??????????????????????????????????????????Id
        ArrayList<String> stringArryList = new ArrayList<>();
        //?????????????????????Id
        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation groupby_attr = aggregations.getTermsAggregation("groupby_attr");
        List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
        //????????????
        for (TermsAggregation.Entry bucket : buckets) {
            String valueId = bucket.getKey();
            stringArryList.add(valueId);
        }

        skuLsResult.setAttrValueIdList(stringArryList);
        return skuLsResult;
    }

    //?????????????????????dsl??????
    private String makeQueryStringForSearch(SkuLsParams skuLsParams) {
        //?????????????????????
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //??????bool
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //??????keyword ????????????
        if(skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length() > 0) {
            //??????match
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", skuLsParams.getKeyword());
            //??????must
            boolQueryBuilder.must(matchQueryBuilder);

            //????????????
            HighlightBuilder highlighter = searchSourceBuilder.highlighter();

            //?????????????????????
            highlighter.field("skuName");
            highlighter.preTags("<span style=color:red>");
            highlighter.postTags("</span>");

            //?????????????????????????????????????????????
            searchSourceBuilder.highlight(highlighter);

        }


        //?????????????????????Id
        if(skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
            //??????
            for (String valueId : skuLsParams.getValueId()) {
                //??????term
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                //??????filter??? ?????????term
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        //?????? ????????????Id
        if(skuLsParams.getCatalog3Id() != null && skuLsParams.getCatalog3Id().length() > 0){
            //??????term
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", skuLsParams.getCatalog3Id());
            //??????filter??? ?????????term
            boolQueryBuilder.filter(termQueryBuilder);
        }

        //query ??????bool
        searchSourceBuilder.query(boolQueryBuilder);

        //????????????
        //from ????????????????????????
        //
        int from = (skuLsParams.getPageNo()-1)*skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        //size?????????????????????
        searchSourceBuilder.size(skuLsParams.getPageSize());
        //????????????
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        //??????
        //?????????????????? aggs:--terms
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr");
        //"field": "skuAttrValueList.valueId"
        groupby_attr.field("skuAttrValueList.valueId");
        //aggs???????????????
        searchSourceBuilder.aggregation(groupby_attr);

        String query = searchSourceBuilder.toString();
        System.out.println("query:=" + query);
        return query;


    }
}
