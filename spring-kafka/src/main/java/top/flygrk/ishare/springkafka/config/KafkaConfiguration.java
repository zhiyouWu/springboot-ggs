package top.flygrk.ishare.springkafka.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;

import java.util.HashMap;
import java.util.Map;

/**
 * @Package top.flygrk.ishare.springkafka.config
 * @Author wuzy@watone.com.cn
 * @Date 2019/9/10 11:14
 * @Version V1.0
 * @Description:
 */

@Configuration
@EnableKafka
public class KafkaConfiguration {

    // ConcurrentKafkaListenerContainerFactory 为创建kafka监听器的工程类，这里只配置了消费者
    @Bean
    public ConcurrentKafkaListenerContainerFactory<Integer, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory <>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    // 监听容器
    @Bean
    public KafkaMessageListenerContainer listenerContainer() {
        ContainerProperties properties = new ContainerProperties("topic.quick.bean");
        properties.setGroupId("bean");
        properties.setMessageListener(new MessageListener<Integer, String>() {
            private Logger logger = LoggerFactory.getLogger(this.getClass());

            @Override
            public void onMessage(ConsumerRecord<Integer, String> record) {
                logger.info("topic.quick.bean receive: {}", record.toString());
            }
        });
        return new KafkaMessageListenerContainer(consumerFactory(), properties);
    }

    // 根据consumerProps 填写的参数创建消费者工厂
    @Bean
    public ConsumerFactory<Integer, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<Integer, String>(consumerProps());
    }

    // 根据producerProps 填写的参数创建生产者工厂
    @Bean
    public ProducerFactory<Integer, String> producerFactory() {
        DefaultKafkaProducerFactory factory = new DefaultKafkaProducerFactory<>(producerProps());
        // 增加事务处理
        // factory.transactionCapable();
        // factory.setTransactionIdPrefix("tran-");
        return factory;
    }

//    @Bean
//    public KafkaTransactionManager transactionManager(ProducerFactory producerFactory) {
//        KafkaTransactionManager manager = new KafkaTransactionManager(producerFactory);
//        return manager;
//    }


    // kafkaTemplate 实现了kafka发送接收的功能
    @Primary    // @Primary注解的意思是在拥有多个同类型的Bean时优先使用该Bean, 方便我们使用@Autowired注解自动注入。
    @Bean
    public KafkaTemplate<Integer, String> kafkaTemplate() {
        return new KafkaTemplate<Integer, String>(producerFactory());
    }

    @Bean("defaultKafkaTemplate")
    public KafkaTemplate<Integer, String> defaultKafkaTemplate() {
        KafkaTemplate<Integer, String> template = new KafkaTemplate<Integer, String>(producerFactory());
        template.setDefaultTopic("topic.quick.default");
        return template;
    }


    // 消费者配置参数
    private Map<String, Object> consumerProps() {
        Map<String, Object> props = new HashMap<>();
        // 连接地址
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // GroupId
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "spring.kafka");
        // 是否自动提交
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        // 自动提交的频率
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        // Session超时设置
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
        // 键的反序列化方式
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        // 值的反序列化方式
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return props;
    }

    // 生产者配置参数
    private Map<String, Object> producerProps() {
        Map<String, Object> props = new HashMap<>();
        // 连接地址
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // 重试方式： 0表示不启用重试机制, 1表示启用
        props.put(ProducerConfig.RETRIES_CONFIG, 1);
        // 控制批处理大小，单位为字节
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        // 批量发送，延迟为1ms， 启用该功能能有效减少生产者发送消息次数，从而提高并发量
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        // 生产者可以使用的总内存字节来缓冲等待发送到服务器的记录
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 1024000);
        // 键的序列化方式
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        // 值的序列化方式
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return props;
    }

}
