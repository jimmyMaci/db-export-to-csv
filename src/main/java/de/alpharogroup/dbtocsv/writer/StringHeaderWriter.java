package de.alpharogroup.dbtocsv.writer;

import org.springframework.batch.item.file.FlatFileHeaderCallback;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
import java.io.Writer;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StringHeaderWriter implements FlatFileHeaderCallback {
 
    String header;
 
    @Override
    public void writeHeader(Writer writer) throws IOException {
        writer.write(header);
    }
}