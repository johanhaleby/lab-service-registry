package com.jayway.serviceregistry.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

import static com.mongodb.ServerAddress.defaultHost;
import static org.springframework.boot.autoconfigure.data.MongoRepositoriesAutoConfiguration.MongoProperties;

/**
 * Spring MongoDB automagically (using MongoRepositoriesAutoConfiguration) creates Mongo bean and MongoTemplate if not defined and @EnableMongoRepositories is used.
 *
 * Note that we don't need to inject and configure the mongodb uri this way if we set the spring.data.mongo.uri property to the mongodb uri.
 * However Spring will then default to use the "test" database if the property is left out which is not want we want.
 */
@Configuration
@EnableMongoRepositories("com.jayway.serviceregistry.domain")
class MongoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(MongoConfiguration.class);

    private static final String DEFAULT_DB_URI = "mongodb://127.0.0.1/service-registry";

    @Value("${mongo.connection.url:" + DEFAULT_DB_URI + "}")
    String mongoDbConnectionUri;

    @Autowired
    MongoProperties mongoProperties;

    @PostConstruct
    void configureMongoDbUri() {
        final String uri;
        if (StringUtils.isEmpty(mongoDbConnectionUri)) {
            uri = defaultHost();
        } else {
            uri = mongoDbConnectionUri;
        }
        log.info("Mongo DB URL: {}", uri);
        mongoProperties.setUri(mongoDbConnectionUri);
    }
}
