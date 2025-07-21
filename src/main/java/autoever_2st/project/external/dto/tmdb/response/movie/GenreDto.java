package autoever_2st.project.external.dto.tmdb.response.movie;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class GenreDto {
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("name")
    private String name;

    public GenreDto(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}