package de.alpharogroup.dbtocsv.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class MultiplyDataSourceConfiguration {

    @Bean(name = "friendsdbDataSource")
    @Primary
    @ConditionalOnMissingBean(DataSource.class)
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "brosdbDataSource")
    @ConfigurationProperties(prefix = "spring.brosdb-datasource")
    public DataSource brotherdbDataSource() {
        return DataSourceBuilder.create().build();
    }

}