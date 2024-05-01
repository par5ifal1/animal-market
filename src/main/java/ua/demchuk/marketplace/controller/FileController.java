package ua.demchuk.marketplace.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.demchuk.marketplace.pojo.Animal;
import ua.demchuk.marketplace.service.AnimalService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "File Controller", description = "Handles file uploads and animal data retrieval")
public class FileController {

    private final AnimalService animalService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/uploads", consumes = {"multipart/form-data"})
    @Operation(summary = "Upload a file and save animals to the database")
    public List<Animal> addAnimals(@RequestParam("file") MultipartFile file) throws IOException {

        return animalService.saveToDb(file);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/animals")
    @Operation(summary = "Get animals by filters")
    public Page<Animal> getAnimal(
            @RequestParam(required = false) @Parameter(description = "Type of the animal") String type,
            @RequestParam(required = false) @Parameter(description = "Category of the animal") Integer category,
            @RequestParam(required = false) @Parameter(description = "Sex of the animal") String sex,
            @Parameter(description = "Pageable object for pagination and sorting") Pageable pageable) {
        return animalService.findByFilters(pageable, type, category, sex);
    }
}
