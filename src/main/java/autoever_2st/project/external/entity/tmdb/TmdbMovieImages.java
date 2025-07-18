package autoever_2st.project.external.entity.tmdb;

import autoever_2st.project.common.entity.TimeStamp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tmdb_movie_images")
@Getter
@NoArgsConstructor
public class TmdbMovieImages extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "base_url")
    private String baseUrl;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "ratio")
    private Double ratio;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type")
    private ImageType imageType;

    @Column(name = "iso_639_1")
    private String iso6391;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tmdb_movie_detail_id")
    private TmdbMovieDetail tmdbMovieDetail;

    public TmdbMovieImages(String imageUrl, String baseUrl, Integer width, Integer height, Double ratio, ImageType imageType, String iso6391) {
        this.imageUrl = imageUrl;
        this.baseUrl = baseUrl;
        this.width = width;
        this.height = height;
        this.ratio = ratio;
        this.imageType = imageType;
        this.iso6391 = iso6391;
    }

    public TmdbMovieImages setTmdbMovieDetail(TmdbMovieDetail tmdbMovieDetail){
        if(this.tmdbMovieDetail != null){
            throw new IllegalArgumentException("tmdbMovieDetail이 이미 존재합니다.");
        }
        this.tmdbMovieDetail = tmdbMovieDetail;
        tmdbMovieDetail.addTmdbMovieImages(this);
        return this;
    }


}
