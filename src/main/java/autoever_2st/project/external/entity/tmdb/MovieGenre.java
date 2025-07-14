package autoever_2st.project.external.entity.tmdb;

import autoever_2st.project.common.entity.TimeStamp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movie_genre")
@Getter
@NoArgsConstructor
public class MovieGenre extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "genre_id")
    private Long genreId;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "movieGenre", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieGenreMatch> movieGenreMatches = new ArrayList<>();

    public MovieGenre(Long genreId, String name) {
        this.genreId = genreId;
        this.name = name;
    }

    public MovieGenre addMovieGenreMatch(MovieGenreMatch movieGenreMatch){
        this.movieGenreMatches.add(movieGenreMatch);
        return this;
    }

}
