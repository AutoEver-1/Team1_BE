package autoever_2st.project.external.dto.tmdb.response.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class JobImagesDto {
    @JsonProperty("base_url")
    private String baseUrl;
    @JsonProperty("secure_base_url")
    private String secureBaseUrl;
    @JsonProperty("backdrop_sizes")
    private List<String> backdropSizes;
    @JsonProperty("logo_sizes")
    private List<String> logoSizes;
    @JsonProperty("poster_sizes")
    private List<String> posterSizes;
    @JsonProperty("profile_sizes")
    private List<String> profileSizes;
    @JsonProperty("still_sizes")
    private List<String> stillSizes;
}
