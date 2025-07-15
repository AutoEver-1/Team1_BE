package autoever_2st.project.external.dto.tmdb.common.person;

import autoever_2st.project.external.dto.tmdb.response.person.PersonResponseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class PopularPersonWrapperDto {
    @JsonProperty("page")
    private Integer page;
    
    @JsonProperty("results")
    private List<PersonResponseDto> results;
    
    @JsonProperty("total_pages")
    private Integer totalPages;
    
    @JsonProperty("total_results")
    private Integer totalResults;
}