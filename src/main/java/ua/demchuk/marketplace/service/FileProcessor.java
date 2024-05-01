package ua.demchuk.marketplace.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ua.demchuk.marketplace.pojo.Animal;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Component
public class FileProcessor {
    public static List<Animal> processCsv(MultipartFile csvFile) throws IOException{
        File file = convertMultiPartToFile(csvFile);

        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader();

        MappingIterator<Animal> it = mapper.readerFor(Animal.class).with(schema).readValues(file);

        return it.readAll().stream().filter(AnimalService::validate).toList();
    }

    public  static List<Animal> processXml(MultipartFile xmlFile) throws IOException {
        File file = convertMultiPartToFile(xmlFile);
        XmlMapper xmlMapper = new XmlMapper();

        return xmlMapper.readValue(file, new TypeReference<List<Animal>>() {})
                .stream().filter(AnimalService::validate).toList();
    }

    private static File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/"
                + Objects.requireNonNull(file.getOriginalFilename()));
        file.transferTo(convFile);

        return convFile;
    }
}