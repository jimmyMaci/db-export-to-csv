package de.alpharogroup.dbtocsv.configuration;

import de.alpharogroup.dbtocsv.writer.StringHeaderWriter;
import de.alpharogroup.reflection.ReflectionExtensions;
import lombok.NonNull;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.batch.item.file.transform.LineAggregator;

import java.util.Arrays;
import java.util.stream.Collectors;

public class BatchObjectFactory {

    public static <T> LineAggregator<T> newLineAggregator(final @NonNull Class<T> type, final @NonNull String delimiter) {
        DelimitedLineAggregator<T> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(delimiter);
        FieldExtractor<T> fieldExtractor = newFieldExtractor(type);
        lineAggregator.setFieldExtractor(fieldExtractor);
        return lineAggregator;
    }

    public static <T> LineAggregator<T> newLineAggregator(final @NonNull Class<T> type) {
        return newLineAggregator(type, ";");
    }

    public static <T> FieldExtractor<T> newFieldExtractor(final @NonNull Class<T> type) {
        BeanWrapperFieldExtractor<T> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(ReflectionExtensions.getDeclaredFieldNames(type));
        return extractor;
    }

    public static <T> StringHeaderWriter newStringHeaderWriter(final @NonNull Class<T> type) {
        return new StringHeaderWriter(Arrays.stream(ReflectionExtensions.getDeclaredFieldNames(type))
                .map(String::toUpperCase).collect(Collectors.joining(";")));
    }
}
