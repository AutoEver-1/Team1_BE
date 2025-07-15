package autoever_2st.project.external.entity.tmdb;

import autoever_2st.project.common.entity.TimeStamp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table
@Getter
@NoArgsConstructor
public class TmdbLanguage extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iso_639_1")
    private String iso6391;

    @Column(name = "name")
    private String name;

    @Column(name = "english_name")
    private String englishName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tmdb_configuration_id")
    private TmdbConfiguration tmdbConfiguration;

    public TmdbLanguage(String iso6391, String name, String englishName) {
        this.iso6391 = iso6391;
        this.name = name;
        this.englishName = englishName;
    }

    public TmdbLanguage setTmdbConfiguration(TmdbConfiguration tmdbConfiguration){
        this.tmdbConfiguration = tmdbConfiguration;
        tmdbConfiguration.addTmdbLanguage(this);
        return this;
    }
}
