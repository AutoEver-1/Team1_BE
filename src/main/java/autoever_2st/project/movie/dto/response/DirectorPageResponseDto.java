package autoever_2st.project.movie.dto.response;

import autoever_2st.project.movie.dto.DirectorDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Getter
@NoArgsConstructor
public class DirectorPageResponseDto extends PageResponseDto {

    @JsonProperty("directorList")
    Page<DirectorDto> directorList;

    public DirectorPageResponseDto(Page<DirectorDto> directorList) {
        this.directorList = directorList;
    }
}
