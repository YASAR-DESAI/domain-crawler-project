package com.github.yasar.domaincrawler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class DomainCrawlerService {

    @Autowired
    KafkaTemplate<String, Domain> kafkaTemplate;

    private final String KAFKA_TOPIC = "web-domains" ;

    public void crawl(String name){

    Mono<DomainList> domainListMono = WebClient.create()
            .get()
            .uri("https://api.domainsdb.info/v1/domains/search?domain="+name+"&zone=com")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(DomainList.class);

    domainListMono.subscribe(domainList -> {
        domainList.domains
                .forEach(domain -> {
                    kafkaTemplate.send(KAFKA_TOPIC, domain);
                    System.out.println("Domain message - "+domain.getDomain());
                });

    });

    }


}
