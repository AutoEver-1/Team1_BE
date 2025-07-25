package autoever_2st.project.external.dto.tmdb.common.collection;

import autoever_2st.project.external.dto.tmdb.response.collection.CollectionImageDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class CollectionImagesWrapperDto {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("backdrops")
    private List<CollectionImageDto> backdrops;
}
