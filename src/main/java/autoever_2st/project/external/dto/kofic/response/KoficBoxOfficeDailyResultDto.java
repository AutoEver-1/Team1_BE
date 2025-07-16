package autoever_2st.project.external.dto.kofic.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class KoficBoxOfficeDailyResultDto {
    @JsonProperty("boxofficeType")
    private String boxofficeType;
    @JsonProperty("showRange")
    private String showRange;
    @JsonProperty("dailyBoxOfficeList")
    private KoficBoxOfficeListDto dailyBoxOfficeList;
}
