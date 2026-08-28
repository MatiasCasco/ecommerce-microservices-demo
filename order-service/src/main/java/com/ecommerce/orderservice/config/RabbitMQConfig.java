package com.ecommerce.orderservice.config;

import com.ecommerce.common.event.constants.EventConstants;
import com.ecommerce.common.event.constants.EventRoutingKey;
import com.ecommerce.orderservice.shared.constants.RabbitMQConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean(name= "productExchange")
    public TopicExchange productExchange() {
        return new TopicExchange(EventConstants.PRODUCT_EXCHANGE);
    }

    @Bean(name = "productCreatedQueue")
    public Queue productCreatedQueue() {
        return QueueBuilder
                .durable(RabbitMQConstants.PRODUCT_CREATED_QUEUE)
                .build();
    }

    @Bean(name = "productUpdatedQueue")
    public Queue productUpdatedQueue() {
        return QueueBuilder
                .durable(RabbitMQConstants.PRODUCT_UPDATED_QUEUE)
                .build();
    }

    @Bean(name = "productActivatedQueue")
    public Queue productActivatedQueue() {
        return QueueBuilder
                .durable(RabbitMQConstants.PRODUCT_ACTIVATED_QUEUE)
                .build();
    }

    @Bean(name = "productDeactivatedQueue")
    public Queue productDeactivatedQueue() {
        return QueueBuilder
                .durable(RabbitMQConstants.PRODUCT_DEACTIVATED_QUEUE)
                .build();
    }

    @Bean(name = "productStockQueue")
    public Queue productStockQueue() {
        return QueueBuilder
                .durable(RabbitMQConstants.PRODUCT_STOCK_UPDATED_QUEUE)
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
            @Qualifier("productUpdatedQueue") Queue productUpdatedQueue,
            @Qualifier("productExchange") TopicExchange productExchange
    ) {
        return BindingBuilder
                .bind(productUpdatedQueue)
                .to(productExchange)
                .with(EventRoutingKey.PRODUCT_UPDATED);
    }

    @Bean
    public Binding productActivatedBinding(
            @Qualifier("productActivatedQueue") Queue productActivatedQueue,
            @Qualifier("productExchange")  TopicExchange productExchange
    ) {
        return BindingBuilder
                .bind(productActivatedQueue)
                .to(productExchange)
                .with(EventRoutingKey.PRODUCT_ACTIVATED);
    }

    @Bean
    public Binding productDeactivatedBinding(
            @Qualifier("productDeactivatedQueue") Queue productDeactivatedQueue,
            @Qualifier("productExchange") TopicExchange productExchange
    ) {
        return BindingBuilder
                .bind(productDeactivatedQueue)
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
}
