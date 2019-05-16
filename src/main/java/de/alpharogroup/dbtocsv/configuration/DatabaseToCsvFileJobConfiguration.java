package de.alpharogroup.dbtocsv.configuration;

import javax.sql.DataSource;

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

import de.alpharogroup.dbtocsv.writer.StringHeaderWriter;
import de.alpharogroup.migration.dto.FriendDto;
import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class DatabaseToCsvFileJobConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

	ApplicationProperties applicationProperties;

    @Autowired
    public DataSource dataSource;

    private static final String QUERY_FIND_FRIENDS =
            "SELECT " +
                    "id, " +
                    "firstname, " +
                    "lastname, " +
                    "city " +
                    "FROM friends " +
                    "ORDER BY firstname ASC";


	@Bean
	public FileSystemResource friendsResource() {
		String filePath = applicationProperties.getCsvDir() + "/" + applicationProperties.getFriendsFileName();
		return new FileSystemResource(filePath);
	}

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

        String exportFileHeader = "ID;FIRSTNAME;LASTNAME;CITY";
        StringHeaderWriter headerWriter = new StringHeaderWriter(exportFileHeader);
        csvFileWriter.setHeaderCallback(headerWriter);
        csvFileWriter.setResource(friendsResource());

        LineAggregator<FriendDto> lineAggregator = newFriendLineAggregator();
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

    private LineAggregator<FriendDto> newFriendLineAggregator() {
        DelimitedLineAggregator<FriendDto> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(";");
        FieldExtractor<FriendDto> fieldExtractor = newPersonFieldExtractor();
        lineAggregator.setFieldExtractor(fieldExtractor);
        return lineAggregator;
    }

    private FieldExtractor<FriendDto> newPersonFieldExtractor() {
        BeanWrapperFieldExtractor<FriendDto> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[] {"id", "firstname", "lastname", "city"});
        return extractor;
    }
}