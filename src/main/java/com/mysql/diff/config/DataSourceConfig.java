package com.mysql.diff.config;

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
    @Bean(name = "destDatasource")
    @ConfigurationProperties(prefix = "spring.dest-datasource")
    DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "sourceDatasource")
    @ConfigurationProperties(prefix = "spring.source-datasource")
    DataSource DataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "destJdbcTemplate")
    JdbcTemplate jdbcTemplate(@Qualifier("destDatasource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "sourceJdbcTemplate")
    JdbcTemplate sourceJdbcTemplate(@Qualifier("sourceDatasource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}