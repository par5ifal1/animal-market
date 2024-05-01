package ua.demchuk.marketplace.testRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.demchuk.marketplace.pojo.Animal;

public interface TestH2Repository extends JpaRepository<Animal, Long> {
}
