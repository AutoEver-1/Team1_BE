package autoever_2st.project.example.service.impl;

import autoever_2st.project.example.dao.TestDao;
import autoever_2st.project.example.entity.TestEntity;
import autoever_2st.project.example.service.ExampleService;
import autoever_2st.project.exception.CustomStatus;
import autoever_2st.project.exception.exception_class.business.DataNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ExampleServiceImpl implements ExampleService {
    private final TestDao testDao;

    public ExampleServiceImpl(TestDao testDao) {
        this.testDao = testDao;
    }

    // CRUD

    @Override
    public TestEntity save(String title, String content) {
        TestEntity testEntity = this.testDao.save(new TestEntity(title, content));
        if(testEntity != null){
            return testEntity;
        }else{
            throw new RuntimeException("데이터 저장 실패, 사유 : 이미 있음 등등");
        }
    }

    @Override
    public TestEntity findById(Long id) {
        Optional<TestEntity> testEntity = this.testDao.findById(id);
        if(testEntity.isPresent()){
            return testEntity.get();
        }else{
            throw new DataNotFoundException("해당 데이터가 없습니다 : request Id " + id, CustomStatus.NOT_HAVE_DATA);
            // 혹은 데이터는 없지만 에러는 아니라고 간주한다면 이곳에서 예외를 던지지말고 컨트롤러단에서 ApiResponse.success를 통해 리턴하도록 함.
        }
    }

    @Override
    @Transactional
    public TestEntity update(Long id, String title, String content) {
        Optional<TestEntity> testEntity = this.testDao.findById(id);
        if(testEntity.isPresent()){
            return testEntity.get().updateTitle(title).updateContent(content);
        }else{
            throw new DataNotFoundException("해당 데이터가 없습니다 : request Id " + id, HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public void deleteById(Long id) {
        Optional<TestEntity> testEntity = this.testDao.findById(id);
        if(testEntity.isPresent()){
            boolean isDeleted = this.testDao.deleteById(id);
            if(!isDeleted){
                throw new RuntimeException("데이터 삭제 실패, 사유 : 삭제 실패");
            }
        }else{
            throw new DataNotFoundException("해당 데이터가 없습니다 : request Id " + id, HttpStatus.NOT_FOUND);
        }
    }
}