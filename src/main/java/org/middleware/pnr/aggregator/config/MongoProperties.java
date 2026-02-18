package org.middleware.pnr.aggregator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mongodb")
public class MongoProperties {
    private String host = "localhost";
    private String database = "pnrdb";
    private int port = 27017;
}
