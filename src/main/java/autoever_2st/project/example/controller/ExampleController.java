package autoever_2st.project.example.controller;

import autoever_2st.project.common.dto.ApiResponse;
import autoever_2st.project.example.dto.request.TestRequestDto;
import autoever_2st.project.example.dto.response.TestResponseDto;
import autoever_2st.project.example.entity.TestEntity;
import autoever_2st.project.example.service.ExampleService;
import autoever_2st.project.exception.CustomStatus;
import autoever_2st.project.exception.exception_class.business.DataNotFoundException;
import autoever_2st.project.exception.exception_class.business.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/example")
public class ExampleController {
    private final ExampleService exampleService;

    public ExampleController(ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    @GetMapping("/{id}")
    public ApiResponse<TestResponseDto> getTestById(@PathVariable Long id){
        if(!isValidId(id)){
            throw new ValidationException("id는 0 이상이어야 합니다.", HttpStatus.BAD_REQUEST);
        }

        TestEntity testEntity = this.exampleService.findById(id);

        if(testEntity == null){
            throw new DataNotFoundException("해당 데이터가 없습니다 : request Id " + id, CustomStatus.NOT_HAVE_DATA.getStatus());

        }else{
            TestResponseDto testResponseDto = new TestResponseDto(testEntity.getId(), testEntity.getTitle(), testEntity.getContent());
            return ApiResponse.success(testResponseDto, HttpStatus.OK.value());
        }
    }

    @PostMapping("/test")
    public ApiResponse<TestResponseDto> saveTest(@RequestBody TestRequestDto testRequestDto){
        if(!isValidTitleAndContent(testRequestDto.getTitle(), testRequestDto.getContent())){
            throw new ValidationException("제목과 내용을 제대로 입력해주세요", CustomStatus.INVALID_INPUT.getStatus());
        }

        TestEntity testEntity = this.exampleService.save(testRequestDto.getTitle(), testRequestDto.getContent());
        if(testEntity == null){
            throw new RuntimeException("데이터 저장 실패, 사유 : 이미 있음 등등");
        }else{
            TestResponseDto testResponseDto = new TestResponseDto(testEntity.getId(), testEntity.getTitle(), testEntity.getContent());
            return ApiResponse.success(testResponseDto, HttpStatus.CREATED.value());
        }
    }

    @PatchMapping("/{id}")
    public ApiResponse<TestResponseDto> updateTest(@PathVariable Long id, @RequestBody TestRequestDto testRequestDto){
        if(!isValidId(id)){
            throw new ValidationException("id는 0이상 이어야 합니다.", HttpStatus.BAD_REQUEST.value());
        }

        TestEntity testEntity = this.exampleService.update(id, testRequestDto.getTitle(), testRequestDto.getContent());
        if(testEntity == null){
            throw new DataNotFoundException("해당 데이터가 없습니다 : request Id " + id, CustomStatus.NOT_HAVE_DATA.getStatus());
        }else{
            TestResponseDto testResponseDto = new TestResponseDto(testEntity.getId(), testEntity.getTitle(), testEntity.getContent());
            return ApiResponse.success(testResponseDto, HttpStatus.OK.value());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTest(@PathVariable Long id){
        if(!isValidId(id)){
            throw new ValidationException("id는 0이상 이어야 합니다.", HttpStatus.BAD_REQUEST.value());
        }

        this.exampleService.deleteById(id);
        return ApiResponse.success(null, HttpStatus.NO_CONTENT.value());
    }

    private boolean isValidId(Long id) {
        return id != null && id > 0;
    }

    private boolean isValidTitleAndContent(String title, String content){
        return title != null && !title.isEmpty() && content != null && !content.isEmpty();
    }
}