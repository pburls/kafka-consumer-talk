package com.sky.csc.integrations

import com.sky.csc.Configuration
import com.sky.kaas.factory.ClientFactory
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig;

class CsaPersistedTopics {
    def readkafkaTopic() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Configuration.CsaPersistedTopicsConsumerConfig.bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, Configuration.CsaPersistedTopicsConsumerConfig.groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        Consumer<String, String> consumer = new ClientFactory<String, String>().createKafkaConsumer()

        consumer.subscribe(topics)

        while (true) {
            def consumerRecords = consumer.poll(1000)

            consumerRecords.each{ record ->
                println record.key()
                println record.value()
            }

            consumer.commitAsync();
        }
        consumer.close();
    }
}
