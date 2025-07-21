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
    private List<autoever_2st.project.movie.dto.OttResponseDto> ottList;

    @JsonProperty("netflixMovieList")
    private List<autoever_2st.project.movie.dto.NetflixMovieListResponseDto> netflixMovieList;

    @JsonProperty("watchaMovieList")
    private List<autoever_2st.project.movie.dto.WatchaMovieListResponseDto> watchaMovieList;

    @JsonProperty("disneyPlusMovieList")
    private List<autoever_2st.project.movie.dto.DisneyPlusMovieListResponseDto> disneyPlusMovieList;

    @JsonProperty("waveMovieList")
    private List<autoever_2st.project.movie.dto.WaveMovieListResponseDto> waveMovieList;

    public OttMovieListResponseDto(List<autoever_2st.project.movie.dto.OttResponseDto> ottList, 
                             List<autoever_2st.project.movie.dto.NetflixMovieListResponseDto> netflixMovieList, 
                             List<autoever_2st.project.movie.dto.WatchaMovieListResponseDto> watchaMovieList, 
                             List<autoever_2st.project.movie.dto.DisneyPlusMovieListResponseDto> disneyPlusMovieList, 
                             List<autoever_2st.project.movie.dto.WaveMovieListResponseDto> waveMovieList) {
        this.ottList = ottList;
        this.netflixMovieList = netflixMovieList;
        this.watchaMovieList = watchaMovieList;
        this.disneyPlusMovieList = disneyPlusMovieList;
        this.waveMovieList = waveMovieList;
    }
}
