package autoever_2st.project.external.dto.kofic.common;

import autoever_2st.project.external.dto.kofic.response.KoficBoxOfficeDailyResultDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class KoficDailyBoxOfficeDto {
    @JsonProperty("boxOfficeResult")
    private KoficBoxOfficeDailyResultDto boxOfficeResult;
}
