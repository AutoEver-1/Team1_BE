package autoever_2st.project.external.entity.tmdb;

import autoever_2st.project.common.entity.TimeStamp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tmdb_countries")
@Getter
@NoArgsConstructor
public class TmdbCountries extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iso_3166_1")
    private String iso31661;

    @Column(name = "english_name")
    private String englishName;

    @Column(name = "native_name")
    private String nativeName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tmdb_configuration_id")
    private TmdbConfiguration tmdbConfiguration;

    public TmdbCountries(String iso31661, String englishName, String nativeName) {
        this.iso31661 = iso31661;
        this.englishName = englishName;
        this.nativeName = nativeName;
    }

    public TmdbCountries setTmdbConfiguration(TmdbConfiguration tmdbConfiguration){
        this.tmdbConfiguration = tmdbConfiguration;
        tmdbConfiguration.addTmdbCountries(this);
        return this;
    }
}
