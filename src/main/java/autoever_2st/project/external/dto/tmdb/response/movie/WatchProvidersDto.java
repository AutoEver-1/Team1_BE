package autoever_2st.project.external.dto.tmdb.response.movie;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class WatchProvidersDto {

    @JsonProperty("link")
    private String link;
    @JsonProperty("flatrate")
    private List<ProviderInner> flatRate;
    @JsonProperty("buy")
    private List<ProviderInner> buy;
    @JsonProperty("rent")
    private List<ProviderInner> rent;

    @NoArgsConstructor
    @Getter
    public static class ProviderInner {
        @JsonProperty("display_priority")
        private Integer displayPriority;
        @JsonProperty("logo_path")
        private String logoPath;
        @JsonProperty("provider_id")
        private Integer providerId;
        @JsonProperty("provider_name")
        private String providerName;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProviderInner that = (ProviderInner) o;
            return providerId.equals(that.providerId);
        }

        @Override
        public int hashCode() {
            return providerId != null ? providerId.hashCode() : 0;
        }
    }

}

