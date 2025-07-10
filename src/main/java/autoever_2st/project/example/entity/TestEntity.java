package autoever_2st.project.example.entity;

import autoever_2st.project.common.entity.TimeStamp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "test_table")
@Getter
@NoArgsConstructor
public class TestEntity extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Column
    private String content;

    // Entity 컬럼은 카멜-케이스로 하되, 데이터베이스에 올라 가는 컬럼 명은 스네이크 케이스로 고정할 것.
    // SQL, 특히 MYSQL은 대소문자를 구분하지않음. 따라서 파스칼/카멜-케이스를 사용할 경우 문제가 생길 수 있음.
    // ref - https://stackoverflow.com/questions/16288586/any-reason-to-still-use-snake-case-for-database-tables-and-columns
    @Column(name="hello_world", nullable = true)
    private String helloWorld;

    public TestEntity(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public TestEntity updateTitle(String title){
        if(this.title == null || this.title.isEmpty()){
            throw new IllegalArgumentException("title은 null일 수 없습니다.");
        }
        this.title = title;
        return this;
    }

    public TestEntity updateContent(String content){
        if(this.content == null || this.title.isEmpty()){
            throw new IllegalArgumentException("content은 null일 수 없습니다.");
        }
        this.content = content;
        return this;
    }
}