package autoever_2st.project.external.dto.tmdb.common.person;

import autoever_2st.project.external.dto.tmdb.response.person.CreditDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class TvCreditsPersonWrapperDto {
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("cast")
    private List<CreditDto> cast;
    
    @JsonProperty("crew")
    private List<CreditDto> crew;
}