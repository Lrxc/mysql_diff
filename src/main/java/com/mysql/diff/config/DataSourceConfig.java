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
    @Bean(name = "sourceDatasource")
    @ConfigurationProperties(prefix = "spring.source-datasource")
    DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "targetDatasource")
    @ConfigurationProperties(prefix = "spring.target-datasource")
    DataSource DataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "sourceJdbcTemplate")
    JdbcTemplate sourceJdbcTemplate(@Qualifier("sourceDatasource") DataSource localDataSource) {
        return new JdbcTemplate(localDataSource);
    }

    @Bean(name = "targetJdbcTemplate")
    JdbcTemplate targetJdbcTemplate(@Qualifier("targetDatasource") DataSource ctDataSource) {
        return new JdbcTemplate(ctDataSource);
    }
}