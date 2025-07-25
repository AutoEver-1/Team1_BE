package autoever_2st.project.external.enums;

import lombok.Getter;

@Getter
public enum Gender {
    UNKNOWN(0, "알수없음", "UNKNOWN"), FEMALE(1, "여자", "FEMALE"), MALE(2, "남자", "MALE");

    private Integer genderValue;
    private String genderKrString;
    private String genderEnString;

    Gender(Integer genderValue, String genderKrString, String genderEnString) {
        this.genderValue = genderValue;
        this.genderKrString = genderKrString;
        this.genderEnString = genderEnString;
    }
}
