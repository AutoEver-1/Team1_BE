package autoever_2st.project.batch.reader;

import autoever_2st.project.batch.dto.KoficTmdbMappingDto;
import autoever_2st.project.external.entity.kofic.KoficMovieDetail;
import autoever_2st.project.external.repository.kofic.KoficMovieDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * KOFIC 영화와 TMDB 영화를 매핑하는 Reader
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KoficTmdbMappingBatchReader {

    private final KoficMovieDetailRepository koficMovieDetailRepository;

    /**
     * TMDB 영화와 매핑되지 않은 KOFIC 영화 목록을 조회하는 Reader
     */
    public ItemReader<List<KoficTmdbMappingDto>> koficTmdbMappingReader() {
        // TMDB 영화와 매핑되지 않은 KOFIC 영화 목록 조회
        List<KoficMovieDetail> unmappedKoficMovies = koficMovieDetailRepository.findAllByTmdbMovieDetailIsNull();
        log.info("TMDB 영화와 매핑되지 않은 KOFIC 영화 수: {}", unmappedKoficMovies.size());

        if (unmappedKoficMovies.isEmpty()) {
            log.info("매핑되지 않은 KOFIC 영화가 없습니다.");
            return null;
        }

        // KoficTmdbMappingDto 리스트 생성 (Processor에서 처리하기 위해)
        List<KoficTmdbMappingDto> mappingDtos = new ArrayList<>();
        for (KoficMovieDetail koficMovie : unmappedKoficMovies) {
            mappingDtos.add(new KoficTmdbMappingDto(koficMovie.getName(), koficMovie));
        }

        // Reader 반환
        return new ItemReader<List<KoficTmdbMappingDto>>() {
            private boolean read = false;

            @Override
            public List<KoficTmdbMappingDto> read() {
                if (read) {
                    return null;
                }
                read = true;
                log.info("KOFIC 영화 목록 반환: {} 건", mappingDtos.size());
                return mappingDtos;
            }
        };
    }
}
