package autoever_2st.project.example.repository;

import autoever_2st.project.example.entity.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TestRepository extends JpaRepository<TestEntity, Long> {

    // CRUD
    TestEntity save(TestEntity testEntity);
    Optional<TestEntity> findById(Long id);
    void deleteById(Long id);

}