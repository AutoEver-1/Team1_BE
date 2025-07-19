package autoever_2st.project.external.entity.tmdb;

import autoever_2st.project.common.entity.TimeStamp;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "tmdb_movie_crew")
@Getter
public class TmdbMovieCrew extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tmdb_credit_id")
    private String tmdbCreditId;

    @Column(name = "department")
    private String department;

    @Column(name = "job")
    private String job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tmdb_movie_detail_id")
    private TmdbMovieDetail tmdbMovieDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tmdb_member_id")
    private TmdbMember tmdbMember;

    public TmdbMovieCrew(String tmdbCreditId, String department, String job) {
        this.tmdbCreditId = tmdbCreditId;
        this.department = department;
        this.job = job;
    }

    public TmdbMovieCrew setTmdbMovieDetail(TmdbMovieDetail tmdbMovieDetail){
        if(this.tmdbMovieDetail != null){
            throw new IllegalArgumentException("tmdbMovieDetail이 이미 존재합니다.");
        }
        this.tmdbMovieDetail = tmdbMovieDetail;
        tmdbMovieDetail.addTmdbMovieCrew(this);
        return this;
    }

    public TmdbMovieCrew setTmdbMember(TmdbMember tmdbMember){
        if(this.tmdbMember != null){
            throw new IllegalArgumentException("tmdbMember이 이미 존재합니다.");
        }
        this.tmdbMember = tmdbMember;
        tmdbMember.addTmdbMovieCrew(this);
        return this;
    }
}
