package autoever_2st.project.external.dto.tmdb.common.configuration;

import autoever_2st.project.external.dto.tmdb.response.configuration.JobImagesDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class JobConfigurationDto {
    @JsonProperty("change_keys")
    private List<String> changeKeys;
    @JsonProperty("images")
    private JobImagesDto images;
}
