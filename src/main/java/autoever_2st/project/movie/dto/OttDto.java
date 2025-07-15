package autoever_2st.project.movie.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class OttDto {
    private Integer ottId;
    private String ottName;
    private String logoPath;

    public OttDto(Integer ottId, String ottName, String logoPath) {
        this.ottId = ottId;
        this.ottName = ottName;
        this.logoPath = logoPath;
    }
}
