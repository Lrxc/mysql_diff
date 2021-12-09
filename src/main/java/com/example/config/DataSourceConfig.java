package com.example.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Primary
    @Bean(name = "firstDatasource")
    @ConfigurationProperties(prefix = "spring.first-datasource")
    DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "secondDatasource")
    @ConfigurationProperties(prefix = "spring.second-datasource")
    DataSource DataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * 本地数据源
     */
    @Primary
    @Bean(name = "firstJdbcTemplate")
    JdbcTemplate jdbcTemplate(@Qualifier("firstDatasource") DataSource localDataSource) {
        return new JdbcTemplate(localDataSource);
    }

    /**
     * 本地数据源
     */
    @Bean(name = "secondJdbcTemplate")
    JdbcTemplate secondJdbcTemplate(@Qualifier("secondDatasource") DataSource ctDataSource) {
        return new JdbcTemplate(ctDataSource);
    }
}