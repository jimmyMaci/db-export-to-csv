package de.alpharogroup.dbtocsv.configuration;

import de.alpharogroup.dbtocsv.dto.FriendDto;
import de.alpharogroup.dbtocsv.writer.StringHeaderWriter;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;

@Configuration
public class DatabaseToCsvFileJobConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DataSource dataSource;

    private static final String QUERY_FIND_FRIENDS =
            "SELECT " +
                    "firstname, " +
                    "lastname, " +
                    "city " +
                    "FROM friends " +
                    "ORDER BY firstname ASC";

    @Bean
    ItemReader<FriendDto> databaseToCsvItemReader() {
        JdbcCursorItemReader<FriendDto> databaseReader = new JdbcCursorItemReader<>();

        databaseReader.setDataSource(dataSource);
        databaseReader.setSql(QUERY_FIND_FRIENDS);
        databaseReader.setRowMapper(new BeanPropertyRowMapper<>(FriendDto.class));

        return databaseReader;
    }
    
    @Bean
    ItemWriter<FriendDto> databaseToCsvItemWriter() {
        FlatFileItemWriter<FriendDto> csvFileWriter = new FlatFileItemWriter<>();

        String exportFileHeader = "FIRSTNAME;LASTNAME;CITY";
        StringHeaderWriter headerWriter = new StringHeaderWriter(exportFileHeader);
        csvFileWriter.setHeaderCallback(headerWriter);
        String userhome = System.getProperty("user.home");
        String exportFilePath = userhome + "/tmp/friends.csv";
        csvFileWriter.setResource(new FileSystemResource(exportFilePath));

        LineAggregator<FriendDto> lineAggregator = newPersonLineAggregator();
        csvFileWriter.setLineAggregator(lineAggregator);

        return csvFileWriter;
    }

    @Bean
    public Step databaseToCsvStep() {
        return stepBuilderFactory.get("databaseToCsvStep")
                .<FriendDto, FriendDto>chunk(1)
                .reader(databaseToCsvItemReader())
                .writer(databaseToCsvItemWriter())
                .build();
    }

    @Bean
    Job databaseToCsvJob() {
        return jobBuilderFactory.get("databaseToCsvJob")
                .incrementer(new RunIdIncrementer())
                .flow(databaseToCsvStep())
                .end()
                .build();
    }

    private LineAggregator<FriendDto> newPersonLineAggregator() {
        DelimitedLineAggregator<FriendDto> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(";");
        FieldExtractor<FriendDto> fieldExtractor = newPersonFieldExtractor();
        lineAggregator.setFieldExtractor(fieldExtractor);
        return lineAggregator;
    }

    private FieldExtractor<FriendDto> newPersonFieldExtractor() {
        BeanWrapperFieldExtractor<FriendDto> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[] {"firstname", "lastname", "city"});
        return extractor;
    }
}