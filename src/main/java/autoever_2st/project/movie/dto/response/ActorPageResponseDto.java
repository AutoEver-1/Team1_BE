package autoever_2st.project.movie.dto.response;

import autoever_2st.project.movie.dto.ActorDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Getter
@NoArgsConstructor
public class ActorPageResponseDto extends PageResponseDto {

    @JsonProperty("actorList")
    Page<ActorDto> actorList;

    public ActorPageResponseDto(Page<ActorDto> actorList) {
        this.actorList = actorList;
    }

}
