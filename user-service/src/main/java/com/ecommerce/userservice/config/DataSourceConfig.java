package com.ecommerce.userservice.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
public class DataSourceConfig {
    @Bean
    public DataSource dataSource() {

        HikariDataSource ds = new HikariDataSource();

        // Hikari Config
        ds.setPoolName("UserServiceHikariPool");
        ds.setMaximumPoolSize(10);
        ds.setMinimumIdle(3);
        ds.setIdleTimeout(30000);
        ds.setMaxLifetime(1800000);
        ds.setConnectionTimeout(30000);
        ds.setLeakDetectionThreshold(20000);
        ds.setConnectionTestQuery("SELECT 1");

        return ds;
    }
}
