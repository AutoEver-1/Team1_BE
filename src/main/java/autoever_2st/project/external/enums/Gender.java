package autoever_2st.project.external.enums;

import lombok.Getter;

@Getter
public enum Gender {
    UNKNOWN(0, "알수없음"), MALE(1, "남자"), FEMALE(2, "여자");

    private Integer genderValue;
    private String genderKrString;

    Gender(Integer genderValue, String genderKrString) {
        this.genderValue = genderValue;
        this.genderKrString = genderKrString;
    }
}
