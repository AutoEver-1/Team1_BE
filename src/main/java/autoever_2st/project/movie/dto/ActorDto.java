package autoever_2st.project.movie.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ActorDto {
    private String name;
    private String character;
    private String profilePath;

    public ActorDto(String name, String character, String profilePath) {
        this.name = name;
        this.character = character;
        this.profilePath = profilePath;
    }
}