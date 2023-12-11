package org.example.vectordbs.ada_aais;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

class AdaOnAAISExample {
    WebClient aOAI;
    WebClient aIS;

    AdaOnAAISExample(Config config) {
        aOAI = WebClient.builder()
                .baseUrl(config.getOai().getEndpoint())
                .filter(ExchangeFilterFunction.ofRequestProcessor(r ->
                        Mono.just(ClientRequest.from(r)
                                .header("api-key", config.getOai().getKey())
                                .build())))
                .build();

        aIS = WebClient.builder()
                .baseUrl(config.getAis().getEndpoint())
                .filter(ExchangeFilterFunction.ofRequestProcessor(r ->
                        Mono.just(ClientRequest.from(r)
                                .header("api-key", config.getAis().getQueryKey())
                                .build())))
                .build();
    }

    EmbeddingResponse embed(ModelConfig modelConfig, String value) {
        return aOAI.post()
                .uri("/openai/deployments/{deployment}/embeddings?api-version=2023-05-15")
                .bodyValue(Map.of("input", value))
                .retrieve()
                .bodyToMono(EmbeddingResponse.class)
                .block();
    }

    SearchResponse search(String index, SearchRequest request) {
        return aIS.post()
                .uri("indexes('{index}')/docs/search.post.search?api-version=2023-11-01", index)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(SearchResponse.class)
                .block()
                ;
    }

    @Data
    @Accessors(chain = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class EmbeddingResponse {
        // String object; // "list"
        String model;
        List<Data> data;

        @lombok.Data
        @Accessors(chain = true)
        static class Data {
            // String object; // "embedding"
            Integer index;
            float[] embedding;
        }
    }

    @Data
    @Accessors(chain = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SearchResponse {
        @JsonProperty("@odata.count")
        Integer count;
        @JsonProperty("@search.facets")
        List<Facet> facets;
        @JsonProperty("@odata.nextLink")
        String nextLink;

        @JsonProperty("@search.nextPageParameters")
        Map<String, Object> nextPageParameters;


        @Data
        @Accessors(chain = true)
        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Value {
            @JsonProperty("@search.score")
            float score;

            @JsonProperty("@search.highlights")
            Map<String, List<String>> highlights;
            String description;
            String docId;
            String title;
        }


        @Data
        @Accessors(chain = true)
        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Facet {
            List<Category> category;

            @Data
            @Accessors(chain = true)
            static class Category {
                Integer count;
                String value;
            }
        }
    }

    /**
     * @see <a href=https://learn.microsoft.com/en-us/rest/api/searchservice/documents/search-post?view=rest-searchservice-2023-11-01&tabs=HTTP#searchindexsearchdocumentspost>docs</a>
     */
    @Data
    @Accessors(chain = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SearchRequest {
        Boolean count;
        List<String> facets;
        String filter;
        String highlight;
        String highlightPostTag;
        String highlightPreTag;
        Double minimumCoverage; // ? null in example
        String orderby;
        String queryType;
        List<String> scoringParameters;
        String scoringProfile;
        String search;
        String searchFields;
        String searchMode;
        String sessionId;
        String scoringStatistics;
        String select;
        Integer skip;
        Integer top;
        List<VectorQuery> vectorQueries;
        String vectorFilterMode;

        @Data
        @Accessors(chain = true)
        @JsonIgnoreProperties(ignoreUnknown = true)
        static class VectorQuery {
            String kind = "vector";
            float[] vector;
            String fields = "descriptionEmbedding";
            Integer k;
            Boolean exhaustive;
        }
    }

    @Data
    @Accessors(chain = true)
    static class ModelConfig {
        String deployment;
    }

    @Data
    @Accessors(chain = true)
    static class Config {
        Oai oai;
        Ais ais;

        @Data
        @Accessors(chain = true)
        static class Oai {
            String key = "";
            String endpoint = "";
        }

        @Data
        @Accessors(chain = true)
        static class Ais {
            String queryKey = "";
            String endpoint = "";
        }
    }
}
