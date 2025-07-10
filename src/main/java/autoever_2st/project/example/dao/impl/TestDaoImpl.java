package autoever_2st.project.example.dao.impl;

import autoever_2st.project.example.dao.TestDao;
import autoever_2st.project.example.entity.TestEntity;
import autoever_2st.project.example.repository.TestRepository;
import autoever_2st.project.exception.ExceptionHandling;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TestDaoImpl implements TestDao {

    private final TestRepository testRepository;

    public TestDaoImpl(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    // CRUD

    @Override
    public TestEntity save(TestEntity testEntity) {
        return ExceptionHandling.executeWithException(() -> testRepository.save(testEntity), "데이터 저장 실패");
    }

    @Override
    public Optional<TestEntity> findById(Long id) {
        return ExceptionHandling.executeWithException(() -> testRepository.findById(id), "데이터 조회 실패");
    }

    @Override
    public boolean deleteById(Long id) {
        return ExceptionHandling.executeWithException(() -> {
            Optional<TestEntity> testOptionalEntity = testRepository.findById(id);
            if(testOptionalEntity.isPresent()){
                testRepository.deleteById(id);
                return true;
            } else {
                return false;
            }
        }, "데이터 삭제 중 오류 발생");
    }
}