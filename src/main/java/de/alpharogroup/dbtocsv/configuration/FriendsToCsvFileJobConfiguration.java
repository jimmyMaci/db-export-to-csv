package de.alpharogroup.dbtocsv.configuration;

import de.alpharogroup.dbtocsv.dto.FriendDto;
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
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FriendsToCsvFileJobConfiguration {

    JobBuilderFactory jobBuilderFactory;

    StepBuilderFactory stepBuilderFactory;

    DataSource dataSource;

    public FriendsToCsvFileJobConfiguration(@NonNull @Qualifier("friendsdbDataSource") DataSource dataSource, StepBuilderFactory stepBuilderFactory, JobBuilderFactory jobBuilderFactory) {
        this.dataSource = dataSource;
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
    }

    private static final String QUERY_FIND_FRIENDS =
            "SELECT " +
                    "firstname, " +
                    "lastname, " +
                    "city " +
                    "FROM friends " +
                    "ORDER BY firstname ASC";

    @Bean
    ItemReader<FriendDto> friendsToCsvItemReader() {
        JdbcCursorItemReader<FriendDto> databaseReader = new JdbcCursorItemReader<>();

        databaseReader.setDataSource(dataSource);
        databaseReader.setSql(QUERY_FIND_FRIENDS);
        databaseReader.setRowMapper(new BeanPropertyRowMapper<>(FriendDto.class));

        return databaseReader;
    }

    @Bean
    Resource friendsCsvResource() {
        return new FileSystemResource(System.getProperty("user.home") + "/tmp/friends.csv");
    }

    @Bean
    ItemWriter<FriendDto> friendsToCsvItemWriter() {
        FlatFileItemWriter<FriendDto> csvFileWriter = new FlatFileItemWriter<>();
        csvFileWriter.setHeaderCallback(BatchObjectFactory.newStringHeaderWriter(FriendDto.class));
        csvFileWriter.setLineAggregator(BatchObjectFactory.newLineAggregator(FriendDto.class));
        csvFileWriter.setResource(friendsCsvResource());
        return csvFileWriter;
    }

    @Bean
    public Step friendsToCsvStep() {
        return stepBuilderFactory.get("friendsToCsvStep")
                .<FriendDto, FriendDto>chunk(1)
                .reader(friendsToCsvItemReader())
                .writer(friendsToCsvItemWriter())
                .build();
    }

    @Bean
    Job friendsToCsvJob() {
        return jobBuilderFactory.get("friendsToCsvJob")
                .incrementer(new RunIdIncrementer())
                .flow(friendsToCsvStep())
                .end()
                .build();
    }

}