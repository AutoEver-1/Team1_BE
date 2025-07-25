package autoever_2st.project.external.dto.tmdb.response.movie;

import autoever_2st.project.external.entity.tmdb.ImageType;
import lombok.Getter;

/**
 * 영화 이미지 정보와 타입을 함께 담는 DTO
 * 
 * 이 클래스는 MovieImageDto에 imageType 필드를 추가하여 이미지의 타입 정보를 함께 담습니다.
 */
@Getter
public class MovieImageWithTypeDto {
    private final MovieImageDto imageDto;
    private final ImageType imageType;

    public MovieImageWithTypeDto(MovieImageDto imageDto, ImageType imageType) {
        this.imageDto = imageDto;
        this.imageType = imageType;
    }

    public Double getAspectRatio() {
        return imageDto.getAspectRatio();
    }

    public String getFilePath() {
        return imageDto.getFilePath();
    }

    public Integer getHeight() {
        return imageDto.getHeight();
    }

    public String getIso6391() {
        return imageDto.getIso6391();
    }

    public Double getVoteAverage() {
        return imageDto.getVoteAverage();
    }

    public Long getVoteCount() {
        return imageDto.getVoteCount();
    }

    public Integer getWidth() {
        return imageDto.getWidth();
    }
}