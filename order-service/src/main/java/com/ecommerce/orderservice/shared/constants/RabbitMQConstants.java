package com.ecommerce.orderservice.shared.constants;

public final class RabbitMQConstants {

    private RabbitMQConstants() {}

    public static final String PRODUCT_CREATED_QUEUE = "product.created.queue";

    public static final String PRODUCT_UPDATED_QUEUE = "product.updated.queue";

    public static final String PRODUCT_ACTIVATED_QUEUE = "product.activated.queue";

    public static final String PRODUCT_DEACTIVATED_QUEUE = "product.deactivated.queue";

    public static final String PRODUCT_STOCK_UPDATED_QUEUE = "product.stock.updated.queue";
}
