package com.ecommerce.common.util;

import java.util.Map;
import java.util.StringJoiner;

import com.ecommerce.common.error.ErrorCode;
import org.slf4j.MDC;

public class CommerceLog {

        private final String module;
        private final String event;
        private final String message;
        private final String path;
        private final String traceId;
        private final Map<String, Object> data;

        private CommerceLog(Builder builder) {
            this.module = builder.module;
            this.event = builder.event;
            this.message = builder.message;
            this.path = builder.path;
            this.traceId = builder.traceId;
            this.data = builder.data;
        }

        public static Builder builder() {
            return new Builder();
        }

        @Override
        public String toString() {
            StringJoiner joiner = new StringJoiner(" | ");

            String traceId = MDC.get("traceId");

            if (traceId != null) joiner.add("traceId=" + traceId);
            if (module != null) joiner.add("module=" + module);
            if (event != null) joiner.add("event=" + event);
            if (message != null) joiner.add("message=" + message);
            if (path != null) joiner.add("path=" + path);
            if (traceId != null) joiner.add("traceId=" + traceId);

            if (data != null && !data.isEmpty()) {
                data.forEach((k, v) -> joiner.add(k + "=" + v));
            }

            return joiner.toString();
        }

        //  BUILDER
        public static class Builder {
            private String module;
            private String event;
            private String message;
            private String path;
            private String traceId;
            private Map<String, Object> data;

            public Builder module(String module) {
                this.module = module;
                return this;
            }

            public Builder event(String event) {
                this.event = event;
                return this;
            }

            public Builder message(String message) {
                this.message = message;
                return this;
            }

            public Builder path(String path) {
                this.path = path;
                return this;
            }

            public Builder traceId(String traceId) {
                this.traceId = traceId;
                return this;
            }

            public Builder data(Map<String, Object> data) {
                this.data = data;
                return this;
            }

            public CommerceLog build() {
                return new CommerceLog(this);
            }
        }

    // ================= EXCÈPTION =================
    public static CommerceLog exception(String module, Exception ex, String path, ErrorCode errorCode) {
        return CommerceLog.builder()
                .module(module)
                .event(ex.getClass().getSimpleName())
                .message(ex.getMessage())
                .path(path)
                .data(Map.of(
                        "errorCode", errorCode.getCode(),
                        "status", errorCode.getStatus()
                ))
                .build();
    }

    // ================= INFO =================
    public static CommerceLog info(String module, String event, String message, String path) {
        return builder()
                .module(module)
                .event(event)
                .message(message)
                .path(path)
                .build();
    }

    public static CommerceLog info(String module, String event, String message, String path, Map<String, Object> data) {
        return builder()
                .module(module)
                .event(event)
                .message(message)
                .path(path)
                .data(data)
                .build();
    }

    // ================= DEBUG =================
    public static CommerceLog debug(String module, String event, String message, String path) {
        return builder()
                .module(module)
                .event(event)
                .message(message)
                .path(path)
                .build();
    }

    public static CommerceLog debug(String module, String event, String message, String path, Map<String, Object> data) {
        return builder()
                .module(module)
                .event(event)
                .message(message)
                .path(path)
                .data(data)
                .build();
    }

    // ================= WARN =================
    public static CommerceLog warn(String module, String event, String message, String path) {
        return builder()
                .module(module)
                .event(event)
                .message(message)
                .path(path)
                .build();
    }

    public static CommerceLog warn(String module, String event, String message, String path, Map<String, Object> data) {
        return builder()
                .module(module)
                .event(event)
                .message(message)
                .path(path)
                .data(data)
                .build();
    }

    // ================= ERROR =================
    public static CommerceLog error(String module, String event, String message, String path) {
        return builder()
                .module(module)
                .event(event)
                .message(message)
                .path(path)
                .build();
    }

    public static CommerceLog error(String module, String event, String message, String path, Map<String, Object> data) {
        return builder()
                .module(module)
                .event(event)
                .message(message)
                .path(path)
                .data(data)
                .build();
    }

}
