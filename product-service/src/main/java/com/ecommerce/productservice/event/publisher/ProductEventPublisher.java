package com.ecommerce.productservice.event.publisher;

import com.ecommerce.common.logging.CommerceLog;
import com.ecommerce.common.trace.TraceConstants;
import com.ecommerce.productservice.event.constants.EventConstants;
import com.ecommerce.productservice.event.model.ProductEvent;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductEventPublisher.class);
    private static final String COMPONENT = "PRODUCT_SERVICE";
    private static final String PUBLISHING = "Publishing event";
    private static final String PUBLISHED = "Published event";

    public void publish(ProductEvent event) {
//        String traceId = MDC.get(TraceConstants.TRACE_ID);
//        event.setTraceId(traceId);
        LOGGER.info(CommerceLog.info(
                COMPONENT,
                event.getEventType(),
                PUBLISHING,
                null,
                Map.of(
//                        "traceId", traceId,
                        "eventId", event.getEventId(),
                        "productId", event.getAggregateId(),
                        "occurredAt", event.getOccurredAt()
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
                PUBLISHED,
                null,
                Map.of(
//                        "traceId", traceId,
                        "eventId", event.getEventId(),
                        "productId", event.getAggregateId(),
                        "occurredAt", event.getOccurredAt()
                )
        ).toString());
    }

}
