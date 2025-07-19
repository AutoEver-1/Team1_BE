package autoever_2st.project.external.enums;

import lombok.Getter;

@Getter
public enum Gender {
    UNKNOWN(0, "알수없음", "UNKNOWN"), MALE(1, "남자", "MALE"), FEMALE(2, "여자", "FEMALE");

    private Integer genderValue;
    private String genderKrString;
    private String genderEnString;

    Gender(Integer genderValue, String genderKrString, String genderEnString) {
        this.genderValue = genderValue;
        this.genderKrString = genderKrString;
        this.genderEnString = genderEnString;
    }
}
