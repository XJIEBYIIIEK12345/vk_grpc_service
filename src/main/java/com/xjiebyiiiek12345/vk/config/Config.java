package com.xjiebyiiiek12345.vk.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.tarantool.client.factory.TarantoolFactory;
import io.tarantool.spring.data34.repository.config.EnableTarantoolRepositories;
import io.tarantool.client.crud.TarantoolCrudClient;

@Configuration
@EnableTarantoolRepositories(basePackages = "com.XJIEBYIIIEK12345.vk.repository")
public class Config {
	
	private static final Logger log = LoggerFactory.getLogger(Config.class);
	
	@Bean
    public TarantoolCrudClient tarantoolClient() {
        try {
        	TarantoolCrudClient client = TarantoolFactory.crud()
                    .withHost("localhost")
                    .withPort(3301)
                    .withUser("guest")
                    .withPassword(null)
                    .build();
            
            client.ping().get();
            log.info("Tarantool client successfully initialized and connected");

            return client;
        } catch (Exception e) {
        	log.error("Failed to create Tarantool client. Check server availability and credentials", e);
            throw new RuntimeException("Failed to create Tarantool client", e);
        }
    }
}
