package autoever_2st.project.external.entity.tmdb;

import autoever_2st.project.common.entity.TimeStamp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tmdb_movie_cast")
@Getter
@NoArgsConstructor
public class TmdbMovieCast extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cast_character" , columnDefinition = "TEXT")
    private String castCharacter;

    @Column(name = "cast_order")
    private Long castOrder;

    @Column(name = "tmdb_cast_id")
    private Long tmdbCastId;

    @Column(name = "known_for_department")
    private String knownForDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tmdb_movie_detail_id")
    private TmdbMovieDetail tmdbMovieDetail;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
    @JoinColumn(name = "tmdb_member_id")
    private TmdbMember tmdbMember;

    public TmdbMovieCast(String castCharacter, Long castOrder, Long tmdbCastId, String knownForDepartment) {
        this.castCharacter = castCharacter;
        this.castOrder = castOrder;
        this.tmdbCastId = tmdbCastId;
        this.knownForDepartment = knownForDepartment;
    }

    public TmdbMovieCast setTmdbMovieDetail(TmdbMovieDetail tmdbMovieDetail){
        if(this.tmdbMovieDetail != null){
            throw new IllegalArgumentException("tmdbMovieDetail이 이미 존재합니다.");
        }
        this.tmdbMovieDetail = tmdbMovieDetail;
        tmdbMovieDetail.addTmdbMovieCast(this);
        return this;
    }


    public TmdbMovieCast setTmdbMember(TmdbMember tmdbMember){
        if(this.tmdbMember != null){
            throw new IllegalArgumentException("tmdbMember이 이미 존재합니다.");
        }
        this.tmdbMember = tmdbMember;
        tmdbMember.addTmdbMovieCast(this);
        return this;
    }

}
