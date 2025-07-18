package autoever_2st.project.external.entity.tmdb;

import autoever_2st.project.exception.exception_class.business.BusinessException;
import org.springframework.http.HttpStatus;

public enum ImageType {
    BACKDROP("backdrops"),
    LOGO("logos"),
    POSTER("posters");

    private final String value;

    ImageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ImageType fromValue(String value) {
        for (ImageType type : ImageType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new BusinessException("찾을수 없는 이미지 타입: " + value, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}