package autoever_2st.project.external.entity.tmdb;

import autoever_2st.project.common.entity.TimeStamp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ott_platform")
@Getter
@NoArgsConstructor
public class OttPlatform extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tmdb_ott_id")
    private Long tmdbOttId;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "ottPlatform", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TmdbMovieDetailOtt> tmdbMovieDetailOtt = new ArrayList<>();

    public OttPlatform(Long tmdbOttId, String name) {
        this.tmdbOttId = tmdbOttId;
        this.name = name;
    }
    public OttPlatform addTmdbMovieDetailOtt(TmdbMovieDetailOtt tmdbMovieDetailOtt){
        this.tmdbMovieDetailOtt.add(tmdbMovieDetailOtt);
        return this;
    }
}
