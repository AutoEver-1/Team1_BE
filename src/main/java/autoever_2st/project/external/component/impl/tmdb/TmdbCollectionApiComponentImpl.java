package autoever_2st.project.external.component.impl.tmdb;

import autoever_2st.project.exception.exception_class.business.BusinessException;
import autoever_2st.project.external.dto.tmdb.common.collection.CollectionImagesWrapperDto;
import autoever_2st.project.external.dto.tmdb.common.collection.CollectionWrapperDto;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Qualifier("tmdbCollection")
@Getter
public class TmdbCollectionApiComponentImpl extends TmdbApiComponentImpl{
    private RestClient restClient;

    public TmdbCollectionApiComponentImpl() {
    }

    @PostConstruct
    public void init() {
        this.restClient = getCollectionRestClient();
    }

    public CollectionWrapperDto getCollection(Integer collectionId) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{collectionId}")
                            .queryParam("api_key", getApiKey())
                            .queryParam("language", "ko-KR")
                            .build(collectionId))
                    .retrieve()
                    .body(CollectionWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getCollection 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public CollectionImagesWrapperDto getCollectionImages(Integer collectionId) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{collectionId}/images")
                            .queryParam("api_key", getApiKey())
                            .build(collectionId))
                    .retrieve()
                    .body(CollectionImagesWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getCollectionImages 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
