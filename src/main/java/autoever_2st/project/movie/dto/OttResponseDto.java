package autoever_2st.project.movie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OttResponseDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("ottName")
    private String ottName;

    @JsonProperty("logoPath")
    private String logoPath;
}
