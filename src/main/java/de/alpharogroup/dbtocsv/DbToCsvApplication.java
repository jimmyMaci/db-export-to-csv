package de.alpharogroup.dbtocsv;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableBatchProcessing
@SpringBootApplication
public class DbToCsvApplication {

    public static void main(String[] args) {
        SpringApplication.run(DbToCsvApplication.class, args);
    }

}
