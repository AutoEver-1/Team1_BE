package autoever_2st.project.external.entity.tmdb;

import autoever_2st.project.common.entity.TimeStamp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_company",
       uniqueConstraints = @UniqueConstraint(columnNames = "tmdb_company_id"))
@Getter
@NoArgsConstructor
public class ProductCompany extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tmdb_company_id", unique = true)
    private Long tmdbCompanyId;

    @Column(name = "name")
    private String name;

    @Column(name = "home_page")
    private String homePage;

    @Column(name = "origin_country")
    private String originCountry;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_path")
    private String logoPath;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompanyMovie> companyMovieList = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlternativeCompanyName> alternativeCompanyNameList = new ArrayList<>();


    public ProductCompany(Long tmdbCompanyId, String name, String homePage, String originCountry, String description, String logoPath) {
        this.tmdbCompanyId = tmdbCompanyId;
        this.name = name;
        this.homePage = homePage;
        this.originCountry = originCountry;
        this.description = description;
        this.logoPath = logoPath;
    }

    public ProductCompany addCompanyMovie(CompanyMovie companyMovie){
        this.companyMovieList.add(companyMovie);
        companyMovieList.add(companyMovie);
        return this;
    }

    public ProductCompany addAlternativeCompanyName(AlternativeCompanyName alternativeCompanyName){
        this.alternativeCompanyNameList.add(alternativeCompanyName);
        alternativeCompanyName.setProductCompany(this);
        return this;
    }
}
