package autoever_2st.project.movie.dto.response;

import autoever_2st.project.movie.dto.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class OttMovieListResponseDto {

    @JsonProperty("ottList")
    private List<OttResponseDto> ottList;

    @JsonProperty("netflixMovieList")
    private List<NetflixMovieListResponseDto> netflixMovieList;

    @JsonProperty("watchaMovieList")
    private List<WatchaMovieListResponseDto> watchaMovieList;

    @JsonProperty("disneyPlusMovieList")
    private List<DisneyPlusMovieListResponseDto> disneyPlusMovieList;

    @JsonProperty("waveMovieList")
    private List<WaveMovieListResponseDto> waveMovieList;

    @JsonProperty("tvingMovieList")
    private List<TvingMovieListResponseDto> tvingMovieList;

    @JsonProperty("coupangPlayMovieList")
    private List<CoupangPlayMovieListResponseDto> coupangPlayMovieList;

    public OttMovieListResponseDto(List<OttResponseDto> ottList,
                             List<NetflixMovieListResponseDto> netflixMovieList,
                             List<WatchaMovieListResponseDto> watchaMovieList,
                             List<DisneyPlusMovieListResponseDto> disneyPlusMovieList,
                             List<WaveMovieListResponseDto> waveMovieList,
                             List<TvingMovieListResponseDto> tvingMovieList,
                             List<CoupangPlayMovieListResponseDto> coupangPlayMovieList) {
        this.ottList = ottList;
        this.netflixMovieList = netflixMovieList;
        this.watchaMovieList = watchaMovieList;
        this.disneyPlusMovieList = disneyPlusMovieList;
        this.waveMovieList = waveMovieList;
        this.tvingMovieList = tvingMovieList;
        this.coupangPlayMovieList = coupangPlayMovieList;
    }
}
