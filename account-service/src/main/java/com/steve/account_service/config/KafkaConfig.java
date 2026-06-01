package com.steve.account_service.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
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

   // @Bean
   // public ProducerFactory<String, Object> producerFactory() {
       // Map<String, Object> config = new HashMap<>();

        // Kafka broker
        //config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
             //   "kafka-202d6e1b-multi-tenant-project-management.f.aivencloud.com:16393");

        // Serializers
        //config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
       // config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // SSL Configuration
       // config.put("security.protocol", "SSL");
        //config.put("ssl.keystore.type", "PKCS12");
        //config.put("ssl.keystore.location", "C:/Users/User/Desktop/banking-system/config/keystore.p12");
        //config.put("ssl.keystore.password", "changeit");
        //config.put("ssl.key.password", "changeit");
        //config.put("ssl.truststore.type", "JKS");
        //config.put("ssl.truststore.location", "C:/Users/User/Desktop/banking-system/config/client.truststore.jks");
        //config.put("ssl.truststore.password", "changeit");

        // Optional: Disable type headers for cleaner JSON
        //config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        //return new DefaultKafkaProducerFactory<>(config);
    //}

   // @Bean
    //public KafkaTemplate<String, Object> kafkaTemplate() {
        //return new KafkaTemplate<>(producerFactory());
    //}
}