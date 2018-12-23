package com.leyou.search.controller;

import com.leyou.pojo.Goods;
import com.leyou.pojo.PageResult;
import com.leyou.pojo.SearchRequest;
import com.leyou.pojo.SearchResult;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;
    @PostMapping("page")
    public ResponseEntity<SearchResult> search(@RequestBody SearchRequest searchRequest){
        SearchResult searchResult= searchService.search(searchRequest);
        if (null != searchResult&&0 != searchResult.getItems().size()){
            return ResponseEntity.ok(searchResult);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
