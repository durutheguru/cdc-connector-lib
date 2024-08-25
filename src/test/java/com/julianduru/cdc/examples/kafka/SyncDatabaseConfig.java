package com.julianduru.cdc.examples.kafka;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

/**
 *
 */
@TestConfiguration
@Profile("db-sync")
public class SyncDatabaseConfig {


    @Bean
    public DataSource mysqlDataSource(
        DataProperties dataProperties
    ) {
        var dataSource = new DriverManagerDataSource();

        dataSource.setUrl(dataProperties.getSource().getUrl());
        dataSource.setUsername(dataProperties.getSource().getUsername());
        dataSource.setPassword(dataProperties.getSource().getPassword());
        dataSource.setDriverClassName(dataProperties.getSource().getDriverClassName());

        return dataSource;
    }


    @Bean
    public DataSourceInitializer dataSourceInitializer(DataSource mysqlDataSource) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(mysqlDataSource);

        var populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema.sql"));
        populator.addScript(new ClassPathResource("data.sql"));
        initializer.setDatabasePopulator(populator);

        return initializer;
    }


    @Bean
    public DataSource postgresDataSource(
        DataProperties dataProperties
    ) {
        var dataSource = new DriverManagerDataSource();

        dataSource.setUrl(dataProperties.getSink().getUrl());
        dataSource.setUsername(dataProperties.getSink().getUsername());
        dataSource.setPassword(dataProperties.getSink().getPassword());
        dataSource.setDriverClassName(dataProperties.getSink().getDriverClassName());

        return dataSource;
    }


}
