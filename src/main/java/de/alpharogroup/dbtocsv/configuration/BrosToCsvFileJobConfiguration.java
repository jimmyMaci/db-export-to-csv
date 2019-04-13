package de.alpharogroup.dbtocsv.configuration;

import de.alpharogroup.dbtocsv.dto.BroDto;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BrosToCsvFileJobConfiguration {

    JobBuilderFactory jobBuilderFactory;

    StepBuilderFactory stepBuilderFactory;

    DataSource dataSource;

    public BrosToCsvFileJobConfiguration(@Qualifier("brosdbDataSource") DataSource dataSource, StepBuilderFactory stepBuilderFactory, JobBuilderFactory jobBuilderFactory) {
        this.dataSource = dataSource;
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
    }

    private static final String QUERY_FIND_BROS =
            "SELECT " +
                    "firstname, " +
                    "lastname, " +
                    "city " +
                    "FROM bros " +
                    "ORDER BY firstname ASC";

    @Bean
    ItemReader<BroDto> brosToCsvItemReader() {
        JdbcCursorItemReader<BroDto> databaseReader = new JdbcCursorItemReader<>();

        databaseReader.setDataSource(dataSource);
        databaseReader.setSql(QUERY_FIND_BROS);
        databaseReader.setRowMapper(new BeanPropertyRowMapper<>(BroDto.class));

        return databaseReader;
    }

    @Bean
    Resource brosCsvResource() {
        return new FileSystemResource(System.getProperty("user.home") + "/tmp/bros.csv");
    }

    @Bean
    ItemWriter<BroDto> brosToCsvItemWriter() {
        FlatFileItemWriter<BroDto> csvFileWriter = new FlatFileItemWriter<>();
        csvFileWriter.setHeaderCallback(BatchObjectFactory.newStringHeaderWriter(BroDto.class));
        csvFileWriter.setLineAggregator(BatchObjectFactory.newLineAggregator(BroDto.class));
        csvFileWriter.setResource(brosCsvResource());
        return csvFileWriter;
    }

    @Bean
    public Step brosToCsvStep() {
        return stepBuilderFactory.get("brosToCsvStep")
                .<BroDto, BroDto>chunk(1)
                .reader(brosToCsvItemReader())
                .writer(brosToCsvItemWriter())
                .build();
    }

    @Bean
    Job brosToCsvJob() {
        return jobBuilderFactory.get("brosToCsvJob")
                .incrementer(new RunIdIncrementer())
                .flow(brosToCsvStep())
                .end()
                .build();
    }

}