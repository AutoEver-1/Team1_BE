package autoever_2st.project.external.entity.tmdb;

import autoever_2st.project.common.entity.TimeStamp;
import autoever_2st.project.external.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "tmdb_movie_character")
@Getter
public class TmdbMovieCharacter extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "gender")
    private Gender gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tmdb_movie_detail_id")
    private TmdbMovieDetail tmdbMovieDetail;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "department_id")
    private TmdbDepartment department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private TmdbMember member;

}
