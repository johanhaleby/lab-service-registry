package com.jayway.serviceregistry.boot;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.util.StringUtils;

import java.net.UnknownHostException;

import static com.mongodb.ServerAddress.defaultHost;

/**
 * Spring MongoDB automagically (using MongoRepositoriesAutoConfiguration) creates Mongo bean and MongoTemplate if not defined and @EnableMongoRepositories is used.
 * However since Heroku defines a complete URI to Mongo (including username and pw)
 * we need to override the Mongo bean creation instance and set to URI. But since Spring also creates a MongoTemplate which takes a database name as
 * second parameter it overrides what's defined in the MongoClientURI. Thus we need to create the MongoClientURI as a Spring bean and use it's database name
 * after having overridden Spring's MongoTemplate.
 */
@Configuration
@EnableMongoRepositories("com.jayway.serviceregistry.domain")
class MongoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(MongoConfiguration.class);

    private static final String DEFAULT_DB_URI = "mongodb://127.0.0.1/service-registry";

    @Value("${mongo.connection.url:" + DEFAULT_DB_URI + "}")
    String mongoDbConnectionUri;

    @Bean
    public MongoClientURI mongoClientURI() {
        final String uri;
        if (StringUtils.isEmpty(mongoDbConnectionUri)) {
            uri = defaultHost();
        } else {
            uri = mongoDbConnectionUri;
        }
        log.info("Mongo DB URL: {}", uri);
        return new MongoClientURI(uri);
    }

    @Bean
    public Mongo mongo() throws Exception {
        return new MongoClient(mongoClientURI());
    }

    @Bean
    MongoTemplate mongoTemplate(Mongo mongo) throws UnknownHostException {
        return new MongoTemplate(mongo, mongoClientURI().getDatabase());
    }

}
