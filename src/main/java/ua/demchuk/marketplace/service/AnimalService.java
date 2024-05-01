package ua.demchuk.marketplace.service;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ua.demchuk.marketplace.enums.AnimalCategory;
import ua.demchuk.marketplace.exceptions.UnsupportedFileTypeException;
import ua.demchuk.marketplace.pojo.Animal;
import ua.demchuk.marketplace.repository.AnimalRepository;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static ua.demchuk.marketplace.service.FileProcessor.*;

@RequiredArgsConstructor
@Service
public class AnimalService {
    private final AnimalRepository animalRepository;

    public List<Animal> saveToDb(MultipartFile file) throws IOException {
        List<Animal> animals;

        if (Objects.requireNonNull(file.getOriginalFilename()).toLowerCase().endsWith(".csv")) {
            animals = processCsv(file);
        } else if (file.getOriginalFilename().toLowerCase().endsWith(".xml")) {
            animals = processXml(file);
        }else{
            throw new UnsupportedFileTypeException("Invalid file format " + file.getOriginalFilename());
        }

        return animalRepository.saveAll(animals);
    }


    public static boolean validate(Animal animal) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        return validator.validate(animal).isEmpty();
    }

    public Page<Animal> findByFilters(Pageable pageable, String type, Integer category, String sex) {
        Page<Animal> page = animalRepository.findAllBy(pageable);

        List<Animal> list = page.stream().filter(animal ->
                (type == null || animal.getType().equals(type))
                && (category == null || animal.getCategory().equals(AnimalCategory.getCategoryForInteger(category)))
                && (sex == null || animal.getSex().equals(sex))
        ).toList();

        return new PageImpl<>(list, pageable, list.size());

    }
}
