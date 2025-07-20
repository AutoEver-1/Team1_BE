package autoever_2st.project.batch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 영화-제작사 매핑 정보를 담는 DTO
 */
@Getter
@NoArgsConstructor
public class CompanyMovieMappingDto {
    private Long tmdbMovieId;
    private String movieTitle;
    private List<CompanyInfo> productionCompanies;

    public CompanyMovieMappingDto(Long tmdbMovieId, String movieTitle, List<CompanyInfo> productionCompanies) {
        this.tmdbMovieId = tmdbMovieId;
        this.movieTitle = movieTitle;
        this.productionCompanies = productionCompanies;
    }

    @Getter
    @NoArgsConstructor
    public static class CompanyInfo {
        private Long tmdbCompanyId;
        private String name;

        public CompanyInfo(Long tmdbCompanyId, String name) {
            this.tmdbCompanyId = tmdbCompanyId;
            this.name = name;
        }
    }
} 