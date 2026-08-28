package com.ecommerce.orderservice.adapter.in.consumer;

import com.ecommerce.common.event.model.*;
import com.ecommerce.common.logging.CommerceLog;
import com.ecommerce.common.trace.TraceConstants;
import com.ecommerce.orderservice.shared.constants.RabbitMQConstants;
import com.ecommerce.orderservice.application.port.in.ProductCatalogSynchronizationUseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductCatalogConsumer {

    private final ProductCatalogSynchronizationUseCase productCatalogSynchronizationUseCase;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductCatalogConsumer.class);
    private static final String COMPONENT = "ORDER_SERVICE";

    @RabbitListener(queues = RabbitMQConstants.PRODUCT_CREATED_QUEUE)
    public void onProductCreated(ProductCreatedEvent productCreatedEvent) {
        executeWithTraceId(
                productCreatedEvent.getTraceId(),
                () -> {
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
                    productCatalogSynchronizationUseCase.createProduct(productCreatedEvent);
                    LOGGER.info(
                            CommerceLog.info(
                                    COMPONENT,
                                    "PRODUCT_CATALOG_SYNCHRONIZED",
                                    "Product catalog synchronized.",
                                    RabbitMQConstants.PRODUCT_CREATED_QUEUE,
                                    Map.of(
                                            "eventType", productCreatedEvent.getEventType(),
                                            "eventId", productCreatedEvent.getEventId(),
                                            "aggregateId", productCreatedEvent.getAggregateId(),
                                            "traceId", productCreatedEvent.getTraceId()
                                    )
                            ).toString()
                    );
                }
        );
    }

    @RabbitListener(queues = RabbitMQConstants.PRODUCT_UPDATED_QUEUE)
    public void onProductUpdated(ProductUpdatedEvent productUpdatedEvent) {
        executeWithTraceId(
                productUpdatedEvent.getTraceId(),
                () -> {
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
                    productCatalogSynchronizationUseCase.updateProduct(productUpdatedEvent);
                    LOGGER.info(
                            CommerceLog.info(
                                    COMPONENT,
                                    "PRODUCT_CATALOG_SYNCHRONIZED",
                                    "Product catalog synchronized.",
                                    RabbitMQConstants.PRODUCT_UPDATED_QUEUE,
                                    Map.of(
                                            "eventType", productUpdatedEvent.getEventType(),
                                            "eventId", productUpdatedEvent.getEventId(),
                                            "aggregateId", productUpdatedEvent.getAggregateId(),
                                            "traceId", productUpdatedEvent.getTraceId()
                                    )
                            ).toString()
                    );
                }
        );
    }

    @RabbitListener(queues = RabbitMQConstants.PRODUCT_ACTIVATED_QUEUE)
    public void onProductActivated(ProductActivatedEvent productActivatedEvent) {
        executeWithTraceId(
                productActivatedEvent.getTraceId(),
                () -> {
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
                    productCatalogSynchronizationUseCase.activateProduct(productActivatedEvent);
                    LOGGER.info(
                            CommerceLog.info(
                                    COMPONENT,
                                    "PRODUCT_CATALOG_SYNCHRONIZED",
                                    "Product catalog synchronized.",
                                    RabbitMQConstants.PRODUCT_ACTIVATED_QUEUE,
                                    Map.of(
                                            "eventType", productActivatedEvent.getEventType(),
                                            "eventId", productActivatedEvent.getEventId(),
                                            "aggregateId", productActivatedEvent.getAggregateId(),
                                            "traceId", productActivatedEvent.getTraceId()
                                    )
                            ).toString()
                    );
                }
        );
    }

    @RabbitListener(queues = RabbitMQConstants.PRODUCT_DEACTIVATED_QUEUE)
    public void onProductDeactivated(ProductDeactivatedEvent productDeactivatedEvent) {
        executeWithTraceId(
                productDeactivatedEvent.getTraceId(),
                () -> {
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
                    productCatalogSynchronizationUseCase.deactivateProduct(productDeactivatedEvent);
                    LOGGER.info(
                            CommerceLog.info(
                                    COMPONENT,
                                    "PRODUCT_CATALOG_SYNCHRONIZED",
                                    "Product catalog synchronized.",
                                    RabbitMQConstants.PRODUCT_DEACTIVATED_QUEUE,
                                    Map.of(
                                            "eventType", productDeactivatedEvent.getEventType(),
                                            "eventId", productDeactivatedEvent.getEventId(),
                                            "aggregateId", productDeactivatedEvent.getAggregateId(),
                                            "traceId", productDeactivatedEvent.getTraceId()
                                    )
                            ).toString()
                    );
                }
        );
    }

    @RabbitListener(queues = RabbitMQConstants.PRODUCT_STOCK_UPDATED_QUEUE)
    public void onProductStockUpdated(ProductStockUpdatedEvent productStockUpdatedEvent) {
        executeWithTraceId(
                productStockUpdatedEvent.getTraceId(),
                () -> {
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
                    productCatalogSynchronizationUseCase.updateStock(productStockUpdatedEvent);
                    LOGGER.info(
                            CommerceLog.info(
                                    COMPONENT,
                                    "PRODUCT_CATALOG_SYNCHRONIZED",
                                    "Product catalog synchronized.",
                                    RabbitMQConstants.PRODUCT_STOCK_UPDATED_QUEUE,
                                    Map.of(
                                            "eventType", productStockUpdatedEvent.getEventType(),
                                            "eventId", productStockUpdatedEvent.getEventId(),
                                            "aggregateId", productStockUpdatedEvent.getAggregateId(),
                                            "traceId", productStockUpdatedEvent.getTraceId()
                                    )
                            ).toString()
                    );
                }
        );
    }

    private void executeWithTraceId(String traceId, Runnable action) {
        try {
            MDC.put(TraceConstants.TRACE_ID, traceId);
            action.run();
        } finally {
            MDC.remove(TraceConstants.TRACE_ID);
        }
    }
}