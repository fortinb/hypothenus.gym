package com.isoceles.hypothenus.gym.admin.papi.config;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoClientSettings.Builder;

@Configuration
public class MongoAppConfig extends AbstractMongoClientConfiguration {

	@Value("${spring.data.mongodb.database}")
	private String databaseName;
	
	@Value("${spring.data.mongodb.uri}")
	private String connectionString;
	
	@Override
	protected String getDatabaseName() {
		return databaseName;
	}
	
	@Override
	protected void configureClientSettings(Builder builder) {

		CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
		
	    builder
	        .applyConnectionString(new ConnectionString(connectionString))
	        .codecRegistry(pojoCodecRegistry);
	  }
}
