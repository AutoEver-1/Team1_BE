package autoever_2st.project.external.dto.tmdb.response.ott;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@Getter
public class OttWrapperDto {

    @JsonProperty("display_priorities")
    private Map<String, Integer> displayPriorities;
    @JsonProperty("display_priority")
    private Integer displayPriority;
    @JsonProperty("logo_path")
    private String logoPath;
    @JsonProperty("provider_name")
    private String providerName;
    @JsonProperty("provider_id")
    private Integer providerId;


}
