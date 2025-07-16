package autoever_2st.project.external.dto.kofic.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class KoficBoxOfficeWeeklyResultDto {
    @JsonProperty("boxofficeType")
    private String boxofficeType;

    @JsonProperty("showRange")
    private String showRange;

    @JsonProperty("yearWeekTime")
    private String yearWeekTime;

    @JsonProperty("weeklyBoxOfficeList")
    private KoficBoxOfficeListDto weeklyBoxOfficeList;
}
