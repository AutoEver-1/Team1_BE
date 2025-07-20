package autoever_2st.project.external.entity.tmdb;

import autoever_2st.project.common.entity.TimeStamp;
import autoever_2st.project.external.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tmdb_member",
       uniqueConstraints = @UniqueConstraint(columnNames = "tmdb_id"))
@Getter
@NoArgsConstructor
public class TmdbMember extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_adult")
    private Boolean isAdult;

    @Column(name = "tmdb_id", unique = true)
    private Long tmdbId;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "name")
    private String name;

    @Column(name = "media_type")
    private String mediaType;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "profile_path")
    private String profilePath;

    @OneToMany(mappedBy = "tmdbMember", orphanRemoval = true)
    private List<TmdbMovieCrew> tmdbMovieCrew = new ArrayList<>();

    @OneToMany(mappedBy = "tmdbMember", orphanRemoval = true)
    private List<TmdbMovieCast> tmdbMovieCast = new ArrayList<>();

    public TmdbMember(Boolean isAdult, Long tmdbId, String originalName, String name, String mediaType, Gender gender, String profilePath) {
        this.isAdult = isAdult;
        this.tmdbId = tmdbId;
        this.originalName = originalName;
        this.name = name;
        this.mediaType = mediaType;
        this.gender = gender;
        this.profilePath = profilePath;
    }

    public TmdbMember addTmdbMovieCrew(TmdbMovieCrew tmdbMovieCrew){
        this.tmdbMovieCrew.add(tmdbMovieCrew);
        return this;
    }

    public TmdbMember removeTmdbMovieCrew(TmdbMovieCrew tmdbMovieCrew){
        this.tmdbMovieCrew.remove(tmdbMovieCrew);
        return this;
    }

    public TmdbMember addTmdbMovieCast(TmdbMovieCast tmdbMovieCast){
        this.tmdbMovieCast.add(tmdbMovieCast);
        return this;
    }

    public TmdbMember removeTmdbMovieCast(TmdbMovieCast tmdbMovieCast){
        this.tmdbMovieCast.remove(tmdbMovieCast);
        return this;
    }

}
