package com.ecommerce.productservice.event.publisher;

import com.ecommerce.common.event.constants.ProducerConstants;
import com.ecommerce.common.logging.CommerceLog;
import com.ecommerce.common.trace.TraceConstants;
import com.ecommerce.common.event.constants.EventConstants;
import com.ecommerce.common.event.model.ProductEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductEventPublisher.class);
    private static final String COMPONENT = "PRODUCT_SERVICE";


    public void publish(ProductEvent event) {
        String traceId = MDC.get(TraceConstants.TRACE_ID);
        event.setTraceId(traceId);

        try {
            String payload = objectMapper.writeValueAsString(event);

            LOGGER.info(CommerceLog.info(
                    COMPONENT,
                    event.getEventType(),
                    ProducerConstants.PUBLISHING,
                    null,
                    Map.of(
//                        "traceId", traceId,
                            "eventId", event.getEventId(),
                            "productId", event.getAggregateId(),
                            "exchange", EventConstants.PRODUCT_EXCHANGE,
                            "routingKey", event.getRoutingKey(),
                            "occurredAt", event.getOccurredAt(),
                            "payload", payload
                    )
            ).toString());

            rabbitTemplate.convertAndSend(
                    EventConstants.PRODUCT_EXCHANGE,
                    event.getRoutingKey(),
                    event
            );

            LOGGER.info(CommerceLog.info(
                    COMPONENT,
                    event.getEventType(),
                    ProducerConstants.PUBLISHED,
                    null,
                    Map.of(
//                        "traceId", traceId,
                            "eventId", event.getEventId(),
                            "productId", event.getAggregateId(),
                            "exchange", EventConstants.PRODUCT_EXCHANGE,
                            "routingKey", event.getRoutingKey(),
                            "occurredAt", event.getOccurredAt(),
                            "payload", payload
                    )
            ).toString());

        } catch (JsonProcessingException e) {
            LOGGER.error(
                    CommerceLog.error(
                            COMPONENT,
                            event.getEventType(),
                            "Error serializing event",
                            null,
                            Map.of(
                                    "eventId", event.getEventId(),
                                    "aggregateId", event.getAggregateId()
                            )
                    ).toString(),
                    e
            );

            throw new RuntimeException(e);
        }



    }

}
