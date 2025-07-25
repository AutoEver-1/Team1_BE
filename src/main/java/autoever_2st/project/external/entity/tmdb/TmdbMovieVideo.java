package autoever_2st.project.external.entity.tmdb;

import autoever_2st.project.common.entity.TimeStamp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tmdb_movie_video")
@Getter
@NoArgsConstructor
public class TmdbMovieVideo extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "base_url")
    private String baseUrl;

    @Column(name = "name")
    private String name;

    @Column(name = "site")
    private String site;

    @Column(name = "video_type")
    private String videoType;

    @Column(name = "iso_639_1")
    private String iso6391;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tmdb_movie_detail_id")
    private TmdbMovieDetail tmdbMovieDetail;

    public TmdbMovieVideo(String videoUrl, String baseUrl, String name, String site, String videoType, String iso6391) {
        this.videoUrl = videoUrl;
        this.baseUrl = baseUrl;
        this.name = name;
        this.site = site;
        this.videoType = videoType;
        this.iso6391 = iso6391;
    }

    public TmdbMovieVideo setTmdbMovieDetail(TmdbMovieDetail tmdbMovieDetail){
        if(this.tmdbMovieDetail != null){
            throw new IllegalArgumentException("tmdbMovieDetail이 이미 존재합니다.");
        }
        this.tmdbMovieDetail = tmdbMovieDetail;
        tmdbMovieDetail.addTmdbMovieVideo(this);
        return this;
    }
}
