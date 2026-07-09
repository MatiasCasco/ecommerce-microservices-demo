package com.ecommerce.productservice.config;

import com.ecommerce.common.event.constants.EventConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean(name= "productExchange")
    public TopicExchange productExchange() {
        return new TopicExchange(EventConstants.PRODUCT_EXCHANGE);
    }



    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
