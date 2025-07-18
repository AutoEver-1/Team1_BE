package autoever_2st.project.external.component.impl.tmdb;

import autoever_2st.project.exception.exception_class.business.BusinessException;
import autoever_2st.project.external.dto.tmdb.common.person.CombinedCreditsPersonWrapperDto;
import autoever_2st.project.external.dto.tmdb.common.person.DetailPersonWrapperDto;
import autoever_2st.project.external.dto.tmdb.common.person.ImagesPersonWrapperDto;
import autoever_2st.project.external.dto.tmdb.common.person.MovieCreditsPersonWrapperDto;
import autoever_2st.project.external.dto.tmdb.common.person.PopularPersonWrapperDto;
import autoever_2st.project.external.dto.tmdb.common.person.TvCreditsPersonWrapperDto;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Qualifier("tmdbPerson")
@Getter
public class TmdbPersonApiComponentImpl extends TmdbApiComponentImpl {
    private RestClient restClient;

    public TmdbPersonApiComponentImpl() {
    }

    @PostConstruct
    public void init() {
        this.restClient = getPersonRestClient();
    }

    public PopularPersonWrapperDto getPopularPersonList(Integer page){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/popular")
                            .queryParam("api_key", getApiKey())
                            .queryParam("language", "ko-KR")
                            .queryParam("page", page)
                            .build())
                    .retrieve()
                    .body(PopularPersonWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getPopularPersonList 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public DetailPersonWrapperDto getDetailPerson(Long personId){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{personId}")
                            .queryParam("api_key", getApiKey())
                            .queryParam("language", "ko-KR")
                            .build(personId))
                    .retrieve()
                    .body(DetailPersonWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getDetailPerson 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public CombinedCreditsPersonWrapperDto getCombinedCredits(Long personId){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{personId}/combined_credits")
                            .queryParam("api_key", getApiKey())
                            .queryParam("language", "ko-KR")
                            .build(personId))
                    .retrieve()
                    .body(CombinedCreditsPersonWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getCombinedCredits 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ImagesPersonWrapperDto getImages(Long personId){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{personId}/images")
                            .queryParam("api_key", getApiKey())
                            .queryParam("language", "ko-KR")
                    .build(personId))
                    .retrieve()
                    .body(ImagesPersonWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getImagesPerson 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public MovieCreditsPersonWrapperDto getMovieCredits(Long personId){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{personId}/movie_credits")
                            .queryParam("api_key", getApiKey())
                            .queryParam("language", "ko-KR")
                    .build(personId))
                    .retrieve()
                    .body(MovieCreditsPersonWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getMovieCredits 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public TvCreditsPersonWrapperDto getTvCredits(Long personId){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{personId}/tv_credits")
                            .queryParam("api_key", getApiKey())
                            .queryParam("language", "ko-KR")
                    .build(personId))
                    .retrieve()
                    .body(TvCreditsPersonWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getTvCredits 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
