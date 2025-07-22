package autoever_2st.project.movie.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class DirectorDto {
    private String gender;
    private Long personId;
    private String name;
    private String originalName;
    //private Double popularity;
    private String profilePath;

    public DirectorDto(String gender, Long personId, String name, String originalName, String profilePath) {
        this.gender = gender;
        this.personId = personId;
        this.name = name;
        this.originalName = originalName;
        //this.popularity = popularity;
        this.profilePath = profilePath;
    }
}