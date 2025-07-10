package autoever_2st.project.example.dao;

import autoever_2st.project.example.entity.TestEntity;

import java.util.Optional;

public interface TestDao {

    // CRUD
    public TestEntity save(TestEntity testEntity);
    public Optional<TestEntity> findById(Long id);
    public boolean deleteById(Long id);

}
