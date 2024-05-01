package ua.demchuk.marketplace;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import ua.demchuk.marketplace.enums.AnimalCategory;
import ua.demchuk.marketplace.pojo.Animal;
import ua.demchuk.marketplace.testRepository.TestH2Repository;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {MarketplaceApplication.class})
@AutoConfigureMockMvc
public class GetSortedAnimalsTests{
    @LocalServerPort
    private int port;
    private String baseUrl = "http://localhost";

    @Autowired
    private MockMvc mockMvc;
    private static RestTemplate restTemplate;


    @Autowired
    private TestH2Repository testH2Repository;

    @BeforeAll
    public static void init(){
        restTemplate = new RestTemplate();
    }

    @BeforeEach
    public void setBaseUrl(){
        baseUrl = baseUrl + ":" + port + "/";
    }

    @AfterEach
    public void tearDown() {
        testH2Repository.deleteAll();
    }

    @Test
    public void testGetAnimalsWithoutFiltersAndSorting() throws Exception {
        File file = new ClassPathResource("animals.csv").getFile();
        MockMultipartFile upload = new MockMultipartFile("file", file.getName(),
                MediaType.TEXT_PLAIN_VALUE, Files.readAllBytes(file.toPath()));

        mockMvc.perform(multipart(baseUrl + "files/uploads").file(upload).file(upload))
                .andExpect(status().isOk());

        List<Animal> actualAnimals = testH2Repository.findAll();
        assertEquals(7, actualAnimals.size());

        ResponseEntity<CustomPageImpl<Animal>> responseEntity = restTemplate.exchange(
                baseUrl + "files/animals",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        List<Animal> animalsFromResponse = Objects.requireNonNull(responseEntity.getBody()).getContent();

        assertEquals(actualAnimals.size(), animalsFromResponse.size());

        for (int i = 0; i < animalsFromResponse.size(); i++) {
            Animal actualAnimal = animalsFromResponse.get(i);
            Animal expectedAnimal = actualAnimals.get(i);

            assertEquals(expectedAnimal.getName(), actualAnimal.getName());
            assertEquals(expectedAnimal.getType(), actualAnimal.getType());
            assertEquals(expectedAnimal.getSex(), actualAnimal.getSex());
            assertEquals(expectedAnimal.getWeight(), actualAnimal.getWeight());
            assertEquals(expectedAnimal.getCost(), actualAnimal.getCost());
        }
    }

    @ParameterizedTest
    @MethodSource("testsForSorting")
    public void testGetAnimalsWithSortingBy(String sortBy, List<String> expectedJsonResponse) throws Exception {
        File file = new ClassPathResource("animals.xml").getFile();
        MockMultipartFile upload = new MockMultipartFile("file", file.getName(),
                MediaType.TEXT_PLAIN_VALUE, Files.readAllBytes(file.toPath()));

        mockMvc.perform(multipart(baseUrl + "files/uploads").file(upload))
                .andExpect(status().isOk());

        List<Animal> animals = testH2Repository.findAll();
        assertEquals(7, animals.size());

        ResponseEntity<CustomPageImpl<Animal>> responseEntity = restTemplate.exchange(
                baseUrl + "files/animals?sort=" + sortBy,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        List<Animal> animalsFromResponse = Objects.requireNonNull(responseEntity.getBody()).getContent();

        for (int i = 0; i < animalsFromResponse.size(); i++) {
            Animal animal = animalsFromResponse.get(i);

            Field field = Animal.class.getDeclaredField(sortBy);
            field.setAccessible(true);

            String actualValue = String.valueOf(field.get(animal));

            assertEquals(expectedJsonResponse.get(i), actualValue);
        }
    }

    @ParameterizedTest
    @MethodSource("testsWithOneFilter")
    public void testGetAnimalsWithFilters(String filter, String value) throws Exception {
        File file = new ClassPathResource("animals.xml").getFile();
        MockMultipartFile upload = new MockMultipartFile("file", file.getName(),
                MediaType.TEXT_PLAIN_VALUE, Files.readAllBytes(file.toPath()));

        mockMvc.perform(multipart(baseUrl + "files/uploads").file(upload))
                .andExpect(status().isOk());

        List<Animal> animals = testH2Repository.findAll();
        assertEquals(7, animals.size());

        ResponseEntity<CustomPageImpl<Animal>> responseEntity = restTemplate.exchange(
                baseUrl + "files/animals?" + filter + "=" + value,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        List<Animal> animalsFromResponse = Objects.requireNonNull(responseEntity.getBody()).getContent();

        for (Animal animal : animalsFromResponse) {
            Field field = Animal.class.getDeclaredField(filter);
            field.setAccessible(true);

            String actualValue = String.valueOf(field.get(animal));

            if (filter.equals("category")) {
                assertEquals(AnimalCategory.getCategoryForInteger(Integer.parseInt(value)).toString(),
                        actualValue);
            } else {
                assertEquals(value, actualValue);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("testsWithAllFilters")
    public void testGetAnimalsWithAllFilters(int expectedAmount,
                                             String type, String category, String sex) throws Exception {
        File file = new ClassPathResource("animals.xml").getFile();
        MockMultipartFile upload = new MockMultipartFile("file", file.getName(),
                MediaType.TEXT_PLAIN_VALUE, Files.readAllBytes(file.toPath()));

        mockMvc.perform(multipart(baseUrl + "files/uploads").file(upload))
                .andExpect(status().isOk());

        List<Animal> animals = testH2Repository.findAll();
        assertEquals(7, animals.size());

        ResponseEntity<CustomPageImpl<Animal>> responseEntity = restTemplate.exchange(
                baseUrl + "files/animals?type="+ type + "&category=" + category + "&sex=" + sex,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        List<Animal> animalsFromResponse = Objects.requireNonNull(responseEntity.getBody()).getContent();

        assertEquals(expectedAmount, animalsFromResponse.size());
    }

    private static Stream<Arguments> testsForSorting() {
        return Stream.of(
                Arguments.of("name",
                        List.of("Milo", "Molly", "Simba","Simon", "Toby", "Tucker", "Zoe")),
                Arguments.of("type",
                        List.of("cat" , "cat", "cat", "cat", "dog", "dog", "dog")),
                Arguments.of("sex",
                        List.of("female", "female", "male", "male","male", "male", "male")),
                Arguments.of("weight",
                        List.of("7", "10", "14", "30", "38", "40", "45")),
                Arguments.of("cost",
                        List.of("14", "17", "44", "49", "51", "57", "59"))
        );
    }

    private static Stream<Arguments> testsWithOneFilter() {
        return Stream.of(
                Arguments.of("type", "cat"),
                Arguments.of("type", "dog"),

                Arguments.of("category", "1"),
                Arguments.of("category", "2"),
                Arguments.of("category", "3"),

                Arguments.of("sex", "female"),
                Arguments.of("sex", "male")
        );
    }

    private static Stream<Arguments> testsWithAllFilters() {
        return Stream.of(
                Arguments.of(1, "cat", "3", "female"),
                Arguments.of(1, "dog", "1", "male"),
                Arguments.of(1, "dog", "1", "female"),
                Arguments.of(3, "cat", "3", "male")
        );
    }
}

