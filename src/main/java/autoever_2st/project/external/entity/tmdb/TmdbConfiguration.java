package autoever_2st.project.external.entity.tmdb;

import autoever_2st.project.common.entity.TimeStamp;
import autoever_2st.project.movie.entity.Movie;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tmdb_configuration")
@Getter
@NoArgsConstructor
public class TmdbConfiguration extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "base_url")
    private String baseUrl;

    @Column(name = "base_url_secure")
    private String baseUrlSecure;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TmdbCountries> tmdbCountriesList = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TmdbLanguage> tmdbLanguageList = new ArrayList<>();


    public TmdbConfiguration(String baseUrl, String baseUrlSecure) {
        this.baseUrl = baseUrl;
        this.baseUrlSecure = baseUrlSecure;
    }

    public TmdbConfiguration addTmdbCountries(TmdbCountries tmdbCountries){
        this.tmdbCountriesList.add(tmdbCountries);
        return this;
    }

    public TmdbConfiguration addTmdbLanguage(TmdbLanguage tmdbLanguage){
        this.tmdbLanguageList.add(tmdbLanguage);
        return this;
    }
}
