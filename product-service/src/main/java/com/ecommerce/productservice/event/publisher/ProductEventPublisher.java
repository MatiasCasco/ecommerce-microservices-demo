package com.ecommerce.productservice.event.publisher;

import com.ecommerce.common.logging.CommerceLog;
import com.ecommerce.productservice.event.model.ProductEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ProductEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductEventPublisher.class);
    private static final String COMPONENT = "PRODUCT_SERVICE";
    private static final String PUBLISHING = "Publishing event";

    public void publish(ProductEvent event) {

        LOGGER.info(CommerceLog.info(
                COMPONENT,
                event.getEventType(),
                PUBLISHING,
                null,
                Map.of(
                        "eventId", event.getEventId(),
                        "productId", event.getProductId(),
                        "occurredAt", event.getOccurredAt()
                )
        ).toString());

    }

}
