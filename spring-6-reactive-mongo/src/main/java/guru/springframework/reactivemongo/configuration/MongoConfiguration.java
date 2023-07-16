package guru.springframework.reactivemongo.configuration;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;

import static guru.springframework.reactivemongo.app.ApplicationConstants.*;
import static java.util.Collections.singletonList;

@Configuration
public class MongoConfiguration extends AbstractReactiveMongoConfiguration {

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create();
    }

    @Override
    protected String getDatabaseName() {
        return "sfg";
    }

    @Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {
        builder.credential(MongoCredential
                        .createCredential(
                                MONGO_DATABASE_USERNAME,
                                MONGO_DATABASE_NAME,
                                MONGO_DATABASE_PASSWORD.toCharArray())
                ).applyToClusterSettings(settings -> {
                    settings.hosts((singletonList(
                            new ServerAddress(MONGO_SERVER_ADDRESS, MONGO_DATABASE_PORT)
                    )));
                });
    }

}
