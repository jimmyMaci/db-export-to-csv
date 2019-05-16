package de.alpharogroup.dbtocsv;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import de.alpharogroup.dbtocsv.configuration.ApplicationProperties;

@EnableBatchProcessing
@SpringBootApplication
@EnableConfigurationProperties({ ApplicationProperties.class })
public class DbToCsvApplication {

    public static void main(String[] args) {
        SpringApplication.run(DbToCsvApplication.class, args);
    }

}
