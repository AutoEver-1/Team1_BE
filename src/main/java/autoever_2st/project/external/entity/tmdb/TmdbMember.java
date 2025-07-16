package autoever_2st.project.external.entity.tmdb;

import autoever_2st.project.common.entity.TimeStamp;
import autoever_2st.project.external.enums.Gender;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tmdb_member")
@Getter
@NoArgsConstructor
public class TmdbMember extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_adult")
    private Boolean isAdult;

    @Column(name = "tmdb_id")
    private Long tmdbId;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "name")
    private String name;

    @Column(name = "media_type")
    private String mediaType;

    @Column(name = "gender")
    private Gender gender;

    @Column(name = "known_for_department")
    private String knownForDepartment;

    @Column(name = "profile_path")
    private String profilePath;

    public TmdbMember(Boolean isAdult, Long tmdbId, String originalName, String name, String mediaType, Gender gender, String knownForDepartment, String profilePath) {
        this.isAdult = isAdult;
        this.tmdbId = tmdbId;
        this.originalName = originalName;
        this.name = name;
        this.mediaType = mediaType;
        this.gender = gender;
        this.knownForDepartment = knownForDepartment;
        this.profilePath = profilePath;
    }
}
