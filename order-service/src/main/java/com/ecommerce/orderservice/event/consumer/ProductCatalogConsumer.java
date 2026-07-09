package com.ecommerce.orderservice.event.consumer;

import com.ecommerce.common.event.constants.ConsumerConstants;
import com.ecommerce.common.event.constants.EventConstants;
import com.ecommerce.common.event.model.*;
import com.ecommerce.common.logging.CommerceLog;
import com.ecommerce.orderservice.event.constants.RabbitMQConstants;
import com.ecommerce.orderservice.service.ProductCatalogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductCatalogConsumer {

    private final ProductCatalogService productCatalogService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductCatalogConsumer.class);
    private static final String COMPONENT = "ORDER_SERVICE";

    @RabbitListener(queues = RabbitMQConstants.PRODUCT_CREATED_QUEUE)
    public void onProductCreated(ProductCreatedEvent productCreatedEvent) {
        LOGGER.info(
                CommerceLog.info(
                        COMPONENT,
                        "PRODUCT_EVENT_RECEIVED",
                        "Product event received.",
                        RabbitMQConstants.PRODUCT_CREATED_QUEUE,
                        Map.of(
                                "eventType", productCreatedEvent.getEventType(),
                                "eventId", productCreatedEvent.getEventId(),
                                "aggregateId", productCreatedEvent.getAggregateId(),
                                "traceId", productCreatedEvent.getTraceId(),
                                "event", productCreatedEvent
                        )
                ).toString()
        );
        productCatalogService.createProduct(productCreatedEvent);
    }

    @RabbitListener(queues = RabbitMQConstants.PRODUCT_UPDATED_QUEUE)
    public void onProductUpdated(ProductUpdatedEvent productUpdatedEvent) {
        LOGGER.info(
                CommerceLog.info(
                        COMPONENT,
                        "PRODUCT_UPDATE_EVENT_RECEIVED",
                        "Product update event received.",
                        RabbitMQConstants.PRODUCT_UPDATED_QUEUE,
                        Map.of(
                                "eventType", productUpdatedEvent.getEventType(),
                                "eventId", productUpdatedEvent.getEventId(),
                                "aggregateId", productUpdatedEvent.getAggregateId(),
                                "traceId", productUpdatedEvent.getTraceId(),
                                "event", productUpdatedEvent
                        )
                ).toString()
        );
        productCatalogService.updateProduct(productUpdatedEvent);
    }

    @RabbitListener(queues = RabbitMQConstants.PRODUCT_ACTIVATED_QUEUE)
    public void onProductActivated(ProductActivatedEvent productActivatedEvent) {
        LOGGER.info(
                CommerceLog.info(
                        COMPONENT,
                        "PRODUCT_ACTIVATE_EVENT_RECEIVED",
                        "Product activate event received.",
                        RabbitMQConstants.PRODUCT_ACTIVATED_QUEUE,
                        Map.of(
                                "eventType", productActivatedEvent.getEventType(),
                                "eventId", productActivatedEvent.getEventId(),
                                "aggregateId", productActivatedEvent.getAggregateId(),
                                "traceId", productActivatedEvent.getTraceId(),
                                "event", productActivatedEvent
                        )
                ).toString()
        );
        productCatalogService.activateProduct(productActivatedEvent);
    }

    @RabbitListener(queues = RabbitMQConstants.PRODUCT_DEACTIVATED_QUEUE)
    public void onProductDeactivated(ProductDeactivatedEvent productDeactivatedEvent) {
        LOGGER.info(
                CommerceLog.info(
                        COMPONENT,
                        "PRODUCT_DEACTIVATED_EVENT_RECEIVED",
                        "Product deactivate event received.",
                        RabbitMQConstants.PRODUCT_DEACTIVATED_QUEUE,
                        Map.of(
                                "eventType", productDeactivatedEvent.getEventType(),
                                "eventId", productDeactivatedEvent.getEventId(),
                                "aggregateId", productDeactivatedEvent.getAggregateId(),
                                "traceId", productDeactivatedEvent.getTraceId(),
                                "event", productDeactivatedEvent
                        )
                ).toString()
        );
        productCatalogService.deactivateProduct(productDeactivatedEvent);
    }

    @RabbitListener(queues = RabbitMQConstants.PRODUCT_STOCK_UPDATED_QUEUE)
    public void onProductStockUpdated(ProductStockUpdatedEvent productStockUpdatedEvent) {
        LOGGER.info(
                CommerceLog.info(
                        COMPONENT,
                        "PRODUCT_STOCK_UPDATE_EVENT_RECEIVED",
                        "Product stock update event received.",
                        RabbitMQConstants.PRODUCT_STOCK_UPDATED_QUEUE,
                        Map.of(
                                "eventType", productStockUpdatedEvent.getEventType(),
                                "eventId", productStockUpdatedEvent.getEventId(),
                                "aggregateId", productStockUpdatedEvent.getAggregateId(),
                                "traceId", productStockUpdatedEvent.getTraceId(),
                                "event", productStockUpdatedEvent
                        )
                ).toString()
        );
        productCatalogService.updateStock(productStockUpdatedEvent);
    }

}