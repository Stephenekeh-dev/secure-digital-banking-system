package com.steve.audit_service.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${KAFKA_BOOTSTRAP_SERVERS}")
    private String bootstrapServers;

    @Value("${KAFKA_SECURITY_PROTOCOL}")
    private String securityProtocol;

    @Value("${KAFKA_SSL_KEYSTORE_TYPE}")
    private String sslKeystoreType;

    @Value("${KAFKA_SSL_KEYSTORE_LOCATION}")
    private String sslKeystoreLocation;

    @Value("${KAFKA_SSL_KEYSTORE_PASSWORD}")
    private String sslKeystorePassword;

    @Value("${KAFKA_SSL_KEY_PASSWORD}")
    private String sslKeyPassword;

    @Value("${KAFKA_SSL_TRUSTSTORE_TYPE}")
    private String sslTruststoreType;

    @Value("${KAFKA_SSL_TRUSTSTORE_LOCATION}")
    private String sslTruststoreLocation;

    @Value("${KAFKA_SSL_TRUSTSTORE_PASSWORD}")
    private String sslTruststorePassword;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // SSL Configuration
        config.put("security.protocol", securityProtocol);
        config.put("ssl.keystore.type", sslKeystoreType);
        config.put("ssl.keystore.location", sslKeystoreLocation);
        config.put("ssl.keystore.password", sslKeystorePassword);
        config.put("ssl.key.password", sslKeyPassword);
        config.put("ssl.truststore.type", sslTruststoreType);
        config.put("ssl.truststore.location", sslTruststoreLocation);
        config.put("ssl.truststore.password", sslTruststorePassword);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
