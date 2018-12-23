package com.leyou.search.client;

import com.leyou.LySearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LySearchService.class)
public class CategoryClientTest {

    @Autowired
    private CategoryClient categoryClient;

    @Test
    public void test(){
        List<String> list = categoryClient.queryNameByIds(Arrays.asList(1L, 2L, 3L));
        System.out.println("list = " + list);
    }
}