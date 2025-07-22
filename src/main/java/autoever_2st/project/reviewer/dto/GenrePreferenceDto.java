package autoever_2st.project.reviewer.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class GenrePreferenceDto {
    private String genre;
    private Integer preference;

    public GenrePreferenceDto(String genre, Integer preference) {
        this.genre = genre;
        this.preference = preference;
    }
}
