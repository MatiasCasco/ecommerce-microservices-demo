package com.ecommerce.common.event.constants;

public final class EventRoutingKey {

    private EventRoutingKey() {}

    public static final String PRODUCT_CREATED = "product.created";

    public static final String PRODUCT_UPDATED = "product.updated";

    public static final String PRODUCT_ACTIVATED = "product.activated";

    public static final String PRODUCT_DEACTIVATED = "product.deactivated";

    public static final String PRODUCT_STOCK_UPDATED = "product.stock.updated";

}
