package com.ecommerce.productservice.config;

import com.ecommerce.productservice.event.constants.EventConstants;
import com.ecommerce.productservice.event.constants.EventRoutingKey;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean(name= "productExchange")
    public TopicExchange productExchange() {
        return new TopicExchange(EventConstants.PRODUCT_EXCHANGE);
    }
    // Colas y Binding deberiamos migrar a Consumer a un futuro
    @Bean(name = "productCreatedQueue")
    public Queue productCreatedQueue() {
        return QueueBuilder
                .durable(EventConstants.PRODUCT_CREATED_QUEUE)
                .build();
    }

    @Bean(name = "productLifeCycleQueue")
    public Queue productLifeCycleQueue() {
        return QueueBuilder
                .durable(EventConstants.PRODUCT_LIFECYCLE_QUEUE)
                .build();
    }

    @Bean(name = "productStockQueue")
    public Queue productStockQueue() {
        return QueueBuilder
                .durable(EventConstants.PRODUCT_STOCK_QUEUE)
                .build();
    }

    @Bean
    public Binding productCreatedBinding(
            @Qualifier("productCreatedQueue")  Queue productCreatedQueue,
            @Qualifier("productExchange") TopicExchange productExchange
    ) {
        return BindingBuilder
                .bind(productCreatedQueue)
                .to(productExchange)
                .with(EventRoutingKey.PRODUCT_CREATED);
    }

    @Bean
    public Binding productUpdatedBinding(
            @Qualifier("productLifeCycleQueue") Queue productLifeCycleQueue,
            @Qualifier("productExchange") TopicExchange productExchange
    ) {
        return BindingBuilder
                .bind(productLifeCycleQueue)
                .to(productExchange)
                .with(EventRoutingKey.PRODUCT_UPDATED);
    }

    @Bean
    public Binding productActivatedBinding(
            @Qualifier("productLifeCycleQueue") Queue productLifeCycleQueue,
            @Qualifier("productExchange")  TopicExchange productExchange
    ) {
        return BindingBuilder
                .bind(productLifeCycleQueue)
                .to(productExchange)
                .with(EventRoutingKey.PRODUCT_ACTIVATED);
    }

    @Bean
    public Binding productDeactivatedBinding(
            @Qualifier("productLifeCycleQueue") Queue productLifeCycleQueue,
            @Qualifier("productExchange") TopicExchange productExchange
    ) {
        return BindingBuilder
                .bind(productLifeCycleQueue)
                .to(productExchange)
                .with(EventRoutingKey.PRODUCT_DEACTIVATED);
    }

    @Bean
    public Binding productStockBinding(
            @Qualifier("productStockQueue") Queue productStockQueue,
            @Qualifier("productExchange") TopicExchange productExchange
    ) {
        return BindingBuilder
                .bind(productStockQueue)
                .to(productExchange)
                .with(EventRoutingKey.PRODUCT_STOCK_UPDATED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
