package ua.demchuk.marketplace;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import ua.demchuk.marketplace.pojo.Animal;
import ua.demchuk.marketplace.testRepository.TestH2Repository;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {MarketplaceApplication.class})
@AutoConfigureMockMvc
public class AddAnimalsTests {
    @LocalServerPort
    private int port;
    private String baseUrl = "http://localhost";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestH2Repository testH2Repository;

    @BeforeEach
    public void setBaseUrl(){
        baseUrl = baseUrl + ":" + port + "/";
    }

    @AfterEach
    public void tearDown() {
        testH2Repository.deleteAll();
    }

    @Test
    public void testAddAnimalsWithCsvFormat() throws Exception {
        File file = new ClassPathResource("animals.csv").getFile();

        MockMultipartFile upload = new MockMultipartFile("file", "file.csv",
                MediaType.TEXT_PLAIN_VALUE,
                Files.readAllBytes(file.toPath()));

        mockMvc.perform(multipart(baseUrl + "files/uploads").file(upload)).andExpect(status().isOk());

        List<Animal> animals = testH2Repository.findAll();

        assertEquals(7, animals.size());

        for (Animal animal : animals) {
            assertNotNull(animal.getName());
            assertNotNull(animal.getType());
            assertNotNull(animal.getSex());
            assertNotNull(animal.getWeight());
            assertNotNull(animal.getCost());
        }
    }

    @Test
    public void testAddAnimalsWithXmlFormat() throws Exception {
        File file = new ClassPathResource("animals.xml").getFile();

        MockMultipartFile upload = new MockMultipartFile("file", "file.xml",
                MediaType.TEXT_PLAIN_VALUE,
                Files.readAllBytes(file.toPath()));

        mockMvc.perform(multipart(baseUrl + "files/uploads").file(upload)).andExpect(status().isOk());

        List<Animal> animals = testH2Repository.findAll();

        assertEquals(7, animals.size());

        for (Animal animal : animals) {
            assertNotNull(animal.getName());
            assertNotNull(animal.getType());
            assertNotNull(animal.getSex());
            assertNotNull(animal.getWeight());
            assertNotNull(animal.getCost());
        }
    }
}
