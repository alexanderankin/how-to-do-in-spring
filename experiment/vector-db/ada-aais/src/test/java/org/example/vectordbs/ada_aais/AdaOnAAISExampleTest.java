package org.example.vectordbs.ada_aais;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClientResponseException;

class AdaOnAAISExampleTest {
    AdaOnAAISExample example;
    AdaOnAAISExample.Config config;

    @BeforeEach
    void setup() {
        config = new AdaOnAAISExample.Config();
        example = new AdaOnAAISExample(config);
    }

    @Test
    void test_embed() {
        var request = new AdaOnAAISExample.SearchRequest()
                .setCount(null)
                .setFacets(null)
                .setFilter(null)
                .setHighlight(null)
                .setHighlightPostTag(null)
                .setHighlightPreTag(null)
                .setMinimumCoverage(null)
                .setOrderby(null)
                .setQueryType(null)
                .setScoringParameters(null)
                .setScoringProfile(null)
                .setSearch(null)
                .setSearchFields(null)
                .setSearchMode(null)
                .setSessionId(null)
                .setScoringStatistics(null)
                .setSelect(null)
                .setSkip(null)
                .setTop(null)
                .setVectorQueries(null)
                .setVectorFilterMode(null)
                ;

        try {
            var search = example.search("realestate-us-sample-index", request);
            System.out.println("response:");
            System.out.println(search);
        } catch (WebClientResponseException r) {
            System.out.println("error:");
            System.out.println(r.getResponseBodyAsString());
        }
    }
}
