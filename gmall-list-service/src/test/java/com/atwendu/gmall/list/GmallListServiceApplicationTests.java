package com.atwendu.gmall.list;


import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

//@RunWith(SpringRunner.class)
@SpringBootTest
class GmallListServiceApplicationTests {

//    @Autowired
//    private JestClient jestClient;

    private static final JestClient jestClient;
    static {
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder("http://192.168.253.219:9200").multiThreaded(true).build());
        jestClient = factory.getObject();
    }

    @Test
    void contextLoads() {
    }

    //测试能否与es连通
    @Test
    public void testES() throws IOException {
        String query = "{\n" +
                "  \"query\": {\n" +
                "    \"term\": {\n" +
                "      \"actorList.name\": \"张译\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        //查询
        Search search = new Search.Builder(query).addIndex("movie_chn").addType("movie").build();


        //执行动作
        SearchResult searchResult = jestClient.execute(search);
        //获取结果
        List<SearchResult.Hit<Map, Void>> hits = searchResult.getHits(Map.class);

        //循环遍历
        for (SearchResult.Hit<Map, Void> hit : hits) {
            Map map = hit.source;
            System.out.println(map.get("name"));    //"红海行动"
        }

    }

}
