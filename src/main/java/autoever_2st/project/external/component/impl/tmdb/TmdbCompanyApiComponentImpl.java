package autoever_2st.project.external.component.impl.tmdb;

import autoever_2st.project.exception.exception_class.business.BusinessException;
import autoever_2st.project.external.dto.tmdb.common.company.AlternativeCompanyTitleWrapperDto;
import autoever_2st.project.external.dto.tmdb.common.company.CompanyImagesWrapperDto;
import autoever_2st.project.external.dto.tmdb.common.company.CompanyWrapperDto;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Qualifier("tmdbCompany")
@Getter
public class TmdbCompanyApiComponentImpl extends TmdbApiComponentImpl {
    private RestClient restClient;

    public TmdbCompanyApiComponentImpl() {
    }

    @PostConstruct
    public void init() {
        this.restClient = getCompanyRestClient();
    }

    public CompanyWrapperDto getCompany(Integer companyId) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{companyId}")
                            .queryParam("api_key", getApiKey())
                            .build(companyId))
                    .retrieve()
                    .body(CompanyWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getCompany 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public AlternativeCompanyTitleWrapperDto getAlternativeCompanyTitle(Integer companyId){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{companyId}/alternative_names")
                            .queryParam("api_key", getApiKey())
                            .build(companyId))
                    .retrieve()
                    .body(AlternativeCompanyTitleWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getAlternativeCompanyTitle 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public CompanyImagesWrapperDto getCompanyImages(Integer companyId){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{companyId}/images")
                            .queryParam("api_key", getApiKey())
                            .build(companyId))
                    .retrieve()
                    .body(CompanyImagesWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getCompanyImages 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
