package com.steve.audit_service.config;

import com.steve.audit_service.dto.AuditEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

// FIX: Original hardcoded kafka:9092 inside the bean — now reads from application.yml
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

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
    public ConsumerFactory<String, AuditEvent> consumerFactory() {
        Map<String, Object> config = new HashMap<>();

        // Basic Consumer Config
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "audit-service-group");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());

        // SSL Configuration
        config.put("security.protocol", securityProtocol);
        config.put("ssl.keystore.type", sslKeystoreType);
        config.put("ssl.keystore.location", sslKeystoreLocation);
        config.put("ssl.keystore.password", sslKeystorePassword);
        config.put("ssl.key.password", sslKeyPassword);
        config.put("ssl.truststore.type", sslTruststoreType);
        config.put("ssl.truststore.location", sslTruststoreLocation);
        config.put("ssl.truststore.password", sslTruststorePassword);
        config.put("ssl.endpoint.identification.algorithm", "https");

        // Deserialization settings
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.steve.audit_service.dto.AuditEvent");

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AuditEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AuditEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}