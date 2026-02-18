package org.middleware.pnr.aggregator.config;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.middleware.pnr.aggregator.exceptions.AggregatorException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Slf4j
public class VertxConfig {

    @Bean
    public Vertx vertx() {
        VertxOptions options = new VertxOptions();
        return Vertx.vertx(options);
    }

    @Bean
    public EventBus eventBus(Vertx vertx) {
        return vertx.eventBus();
    }

    @Bean
    public MongoClient mongoClient(Vertx vertx, MongoProperties mongoProperties) {
        if (mongoProperties.getHost().contains(" ")) {
            throw new AggregatorException(
                    "ERROR: Invalid Mongo host: [" + mongoProperties.getHost() + "]"
            );
        }

        JsonObject config = new JsonObject()
                .put("host", mongoProperties.getHost())
                .put("port", mongoProperties.getPort())
                .put("db_name", mongoProperties.getDatabase())
                .put("maxPoolSize", 20);

        log.info("Creating shared mongo client with config {} ", config.toString());

        return MongoClient.createShared(vertx, config);
    }

}
