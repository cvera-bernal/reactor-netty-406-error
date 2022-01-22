package com.example.test;

import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.test.StepVerifier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

public class RequestTest {

  HttpClient httpClient = HttpClient.create();
  WebClient webClient = WebClient.builder()
      .clientConnector(new ReactorClientHttpConnector(httpClient))
      .build();

  @Test
  void testWebClient200() {
    Mono<ResponseEntity<String>> response = webClient
        .method(HttpMethod.GET)
        .uri("https://johnnycrawfordlegacy.com")
        .headers(headers -> headers.add("User-Agent", "example"))
        .body(BodyInserters.empty())
        .retrieve()
        .toEntity(String.class);

    StepVerifier.create(response)
        .assertNext(e -> Assertions.assertEquals(200, e.getStatusCode().value()))
        .verifyComplete();
  }


  @Test
  void testHttpClient406() {
    Mono<Integer> response = httpClient
        .headers(headers -> headers.add("User-Agent", "example"))
        .get()
        .uri("https://johnnycrawfordlegacy.com")
        .responseSingle((httpResponse, byteBufMono) -> Mono.just(httpResponse.status().code()));

    // The problem is that - HttpClientOperations::newFullBodyMessage add CONTENT_LENGTH=0

    // curl -v -H 'CONTENT-LENGTH: 0' https://johnnycrawfordlegacy.com
    // < HTTP/2 406
    // < content-type: text/html
    // < content-length: 1543
    // < date: Sat, 22 Jan 2022 00:16:09 GMT
    // < server: LiteSpeed

    // curl -v  https://johnnycrawfordlegacy.com
    // < HTTP/2 200
    //< content-type: text/html; charset=UTF-8
    //< link: <https://johnnycrawfordlegacy.com/wp-json/>; rel="https://api.w.org/"
    //< link: <https://johnnycrawfordlegacy.com/wp-json/wp/v2/pages/5092>; rel="alternate"; type="application/json"
    //< link: <https://johnnycrawfordlegacy.com/>; rel=shortlink
    //< date: Sat, 22 Jan 2022 00:17:24 GMT
    //< server: LiteSpeed

    StepVerifier.create(response)
        .assertNext(e -> Assertions.assertEquals(406, e))
        .verifyComplete();
  }
}
