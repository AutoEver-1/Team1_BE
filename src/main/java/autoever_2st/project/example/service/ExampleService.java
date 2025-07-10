package autoever_2st.project.example.service;

import autoever_2st.project.example.entity.TestEntity;

public interface ExampleService {

    // CRUD
    public TestEntity save(String title, String content);

    public TestEntity findById(Long id);

    public TestEntity update(Long id, String title, String content);

    public void deleteById(Long id);

}