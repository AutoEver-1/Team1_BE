package autoever_2st.project.external.entity.tmdb;

import autoever_2st.project.common.entity.TimeStamp;
import autoever_2st.project.movie.entity.Movie;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "company_movie",
       uniqueConstraints = @UniqueConstraint(columnNames = {"movie_id", "product_company_id"}))
@Getter
@NoArgsConstructor
public class CompanyMovie extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_company_id")
    private ProductCompany productCompany;

    public CompanyMovie(Movie movie, ProductCompany productCompany) {
        this.movie = movie;
        movie.addCompanyMovie(this);
        this.productCompany = productCompany;
        productCompany.addCompanyMovie(this);
    }
}
