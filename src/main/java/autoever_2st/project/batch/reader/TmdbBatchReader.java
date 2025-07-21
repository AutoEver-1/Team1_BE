package autoever_2st.project.batch.reader;

import autoever_2st.project.batch.component.TmdbBatchComponent;
import autoever_2st.project.batch.dto.CompanyMovieMappingDto;
import autoever_2st.project.batch.dto.KoficTmdbMappingDto;
import autoever_2st.project.batch.dto.MovieImagesDto;
import autoever_2st.project.batch.dto.MovieVideosDto;
import autoever_2st.project.batch.dto.MovieWatchProvidersDto;
import autoever_2st.project.external.component.impl.tmdb.TmdbMovieApiComponentImpl;
import autoever_2st.project.external.dto.tmdb.common.movie.*;
import autoever_2st.project.external.dto.tmdb.response.movie.*;
import autoever_2st.project.external.dto.tmdb.response.ott.OttWrapperDto;
import autoever_2st.project.external.entity.kofic.KoficMovieDetail;
import autoever_2st.project.external.entity.tmdb.*;
import autoever_2st.project.external.enums.Gender;
import autoever_2st.project.external.repository.kofic.KoficMovieDetailRepository;
import autoever_2st.project.external.repository.tmdb.TmdbMovieDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.Comparator;

/**
 * TMDb API에서 영화 데이터를 페이지 단위로 읽어오는 Reader
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbBatchReader {

    private final TmdbBatchComponent tmdbBatchComponent;
    private final TmdbMovieDetailRepository tmdbMovieDetailRepository;
    private final TmdbMovieApiComponentImpl tmdbMovieApiComponent;
    private final KoficMovieDetailRepository koficMovieDetailRepository;

    // 페이지 크기 상수 - TMDb API의 기본값은 20
    private static final int PAGE_SIZE = 20;

    // 병렬 처리를 위한 스레드 수 - API 제한(10초당 40회)을 고려하여 설정
    private static final int THREAD_COUNT = 20;

    // 배치 크기 - API 제한(10초당 40회)을 고려하여 39로 설정
    private static final int BATCH_SIZE = 39;

    /**
     * 현재 상영중인 영화 데이터를 병렬로 읽어오는 Reader
     * 각 배치(40페이지)를 로드한 후 즉시 반환하여 데이터베이스에 저장하고, 그 후에 다음 배치를 로드하는 방식으로 메모리 사용량을 최적화.
     * 
     * @return 영화 데이터를 반환하는 ItemReader
     */

    public ItemReader<List<MovieResponseDto>> parallelMoviePageReader() {
        // 첫 페이지를 가져와서 총 페이지 수 확인
        DiscoverMovieWrapperDto firstPage = tmdbBatchComponent.getTmdbMovieApiComponent().getDiscoverMovieList(1);
        int totalPages = Math.min(firstPage.getTotalPages(), 40); // 최대 40페이지로 제한

        log.info("현재 상영중인 영화 데이터 로드 시작: 총 {}페이지 중 {}페이지까지 처리 예정", 
                firstPage.getTotalPages(), totalPages);

        List<MovieResponseDto> firstPageResults = firstPage.getResults();

        final AtomicInteger currentBatch = new AtomicInteger(0);
        final int totalBatches = (int) Math.ceil((totalPages - 1) / (double) BATCH_SIZE);

        // 첫 번째 배치는 첫 페이지 결과
        final List<MovieResponseDto> firstBatch = new ArrayList<>(firstPageResults);

        return new ItemReader<List<MovieResponseDto>>() {
            private boolean firstBatchReturned = false;
            private boolean isCompleted = false;
            private ExecutorService executor = null;

            @Override
            public List<MovieResponseDto> read() {
                if (isCompleted) {
                    return null;
                }

                // 첫 번째 배치는 이미 로드된 첫 페이지 결과 반환
                if (!firstBatchReturned) {
                    firstBatchReturned = true;
                    log.info("{}개 항목으로 첫 번째 배치 처리", firstBatch.size());
                    return firstBatch;
                }

                // 현재 배치 번호 가져오기
                int batchIndex = currentBatch.getAndIncrement();

                // 모든 배치를 처리했으면 종료
                if (batchIndex >= totalBatches) {
                    isCompleted = true;
                    if (executor != null) {
                        executor.shutdown();
                    }
                    log.info("현재 상영중인 영화 데이터 로드 완료: 총 {}개 배치 처리됨", totalBatches + 1);
                    return null;
                }

                // 현재 배치의 시작/끝 페이지 계산
                int startPage = batchIndex * BATCH_SIZE + 2; // 첫 페이지는 이미 처리했으므로 2부터 시작
                int endPage = Math.min(startPage + BATCH_SIZE - 1, totalPages);

                log.info("배치 {}/{} 처리 중: 페이지 {} ~ {} 로드", 
                        batchIndex + 1, totalBatches + 1, startPage, endPage);

                // 스레드 풀 생성
                if (executor == null || executor.isShutdown()) {
                executor = Executors.newFixedThreadPool(THREAD_COUNT);
                }

                // 현재 배치의 모든 페이지에 대한 Future 생성
                List<CompletableFuture<PageResult>> futures = new ArrayList<>();
                for (int pageNumber = startPage; pageNumber <= endPage; pageNumber++) {
                    final int page = pageNumber;
                    CompletableFuture<PageResult> future = CompletableFuture.supplyAsync(() -> {
                        List<MovieResponseDto> items = tmdbBatchComponent.fetchMovieDiscover(page);
                        return new PageResult(page, items);
                    }, executor);
                    futures.add(future);
                }

                // 모든 Future 결과 수집 및 정렬
                List<MovieResponseDto> batchResults = new ArrayList<>();
                    for (CompletableFuture<PageResult> future : futures) {
                    try {
                        PageResult result = future.get();
                        if (result != null && result.getItems() != null) {
                            batchResults.addAll(result.getItems());
                    }
                } catch (Exception e) {
                        log.error("페이지 결과를 가져오는 중 오류 발생: {}", e.getMessage(), e);
                    }
                }

                log.info("배치 {}/{} 완료: {}개 항목 로드됨", 
                        batchIndex + 1, totalBatches + 1, batchResults.size());

                return batchResults.isEmpty() ? null : batchResults;
            }
        };
    }

    public ItemReader<List<MovieResponseDto>> parallelUpcomingMoviePageReader() {
        // 첫 페이지를 가져와서 총 페이지 수 확인
        UpComingMovieWrapperDto firstPage = tmdbBatchComponent.getTmdbMovieApiComponent().getUpComingMovieList(1);
        int totalPages = firstPage.getTotalPages() > 500 ? 40 : firstPage.getTotalPages();

        List<MovieResponseDto> firstPageResults = firstPage.getResults();

        final AtomicInteger currentBatch = new AtomicInteger(0);
        final int totalBatches = (int) Math.ceil((totalPages - 1) / (double) BATCH_SIZE);

        // 첫 번째 배치는 첫 페이지 결과
        final List<MovieResponseDto> firstBatch = new ArrayList<>(firstPageResults);

        return new ItemReader<List<MovieResponseDto>>() {
            private boolean firstBatchReturned = false;
            private boolean isCompleted = false;
            private ExecutorService executor = null;

            @Override
            public List<MovieResponseDto> read() {
                if (isCompleted) {
                    return null;
                }

                // 첫 번째 배치는 이미 로드된 첫 페이지 결과 반환
                if (!firstBatchReturned) {
                    firstBatchReturned = true;
                    log.info("{}개 항목으로 첫 번째 배치 처리", firstBatch.size());
                    return firstBatch;
                }

                // 현재 배치 번호 가져오기
                int batchIndex = currentBatch.getAndIncrement();

                // 모든 배치를 처리했으면 종료
                if (batchIndex >= totalBatches) {
                    log.info("Completed processing all {} batches", totalBatches + 1);
                    if (executor != null) {
                        executor.shutdown(); // 모든 작업이 완료되면 스레드 풀 종료
                    }
                    isCompleted = true;
                    return null;
                }

                // 배치 사이에 잠시 대기하여 API 레이트 리밋 준수
                if (batchIndex > 0) {
                    try {
                        Thread.sleep(10000); // 10초 대기
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("Interrupted while waiting between batches", e);
                    }
                }

                // 현재 배치의 페이지 범위 계산
                int startPage = 2 + (batchIndex * BATCH_SIZE);
                int endPage = Math.min(startPage + BATCH_SIZE - 1, totalPages);

                log.info("{} Batch에서 {}로 페이지 배치 {}를 로드.", batchIndex + 1, startPage, endPage);

                // 스레드 풀 생성 - 각 배치마다 새로운 스레드 풀 생성하여 메모리 누수 방지
                executor = Executors.newFixedThreadPool(THREAD_COUNT);

                // 현재 배치의 모든 페이지에 대한 Future 생성
                List<CompletableFuture<PageResult>> futures = new ArrayList<>();

                for (int page = startPage; page <= endPage; page++) {
                    final int currentPage = page;
                    CompletableFuture<PageResult> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            List<MovieResponseDto> pageItems = tmdbBatchComponent.fetchMovieDiscover(currentPage);
                            log.info("페이지 {}/{} 로드됨 - {}개 항목", currentPage, totalPages,
                                    pageItems != null ? pageItems.size() : 0);
                            return new PageResult(currentPage, pageItems != null ? pageItems : new ArrayList<>());
                        } catch (Exception e) {
                            log.error("페이지 {}을(를) 로드하는 중 오류 발생: {}", currentPage, e.getMessage(), e);
                            return new PageResult(currentPage, new ArrayList<>());
                        }
                    }, executor);

                    futures.add(future);

                    // 각 API 호출 사이에 짧은 지연 추가 (레이트 리밋 준수)
                    try {
                        Thread.sleep(50); // 50ms 대기
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("Interrupted while waiting between API calls", e);
                    }
                }

                // 모든 Future 결과 수집하고 페이지 순서대로 정렬
                List<MovieResponseDto> batchItems = new ArrayList<>();
                try {
                    List<PageResult> pageResults = new ArrayList<>();
                    for (CompletableFuture<PageResult> future : futures) {
                        pageResults.add(future.get());
                    }

                    // 페이지 번호순으로 정렬하여 순서 보장
                    pageResults.sort(Comparator.comparingInt(PageResult::getPageNumber));

                    // 정렬된 순서대로 결과 추가
                    for (PageResult pageResult : pageResults) {
                        batchItems.addAll(pageResult.getItems());
                        log.debug("페이지 {} 결과 추가됨 - {}개 항목", pageResult.getPageNumber(), pageResult.getItems().size());
                    }

                    log.info("배치 {} 완료 - 총 {}개 항목 (페이지 {}-{})",
                            batchIndex + 1, batchItems.size(), startPage, endPage);

                } catch (Exception e) {
                    log.error("배치 결과 수집 중 오류 발생: {}", e.getMessage(), e);
                    return new ArrayList<>();
                } finally {
                    if (executor != null) {
                        executor.shutdown();
                    }
                }

                // 빈 배치는 null 반환하여 처리 종료
                if (batchItems.isEmpty()) {
                    log.warn("페이지 {}에서 {}까지 배치에서 항목을 찾을 수 없음.", startPage, endPage);
                    return read(); // 다음 배치 시도
                }

                return batchItems;
            }
        };
    }

//    /**
//     * 인기 영화 ID 목록을 페이지 단위로 읽어오는 Reader
//     */
//    public ItemStreamReader<Integer> popularMovieIdsReader() {
//        // 총 페이지 수를 제한하여 처리 (예: 최대 10페이지)
//        int maxPages = 10;
//
//        log.info("Starting to read popular movies. Max pages: {}, estimated items: {}",
//                maxPages, maxPages * PAGE_SIZE);
//
//        return new PagingPopularMovieReader(maxPages);
//    }

    /**
     * Genre를 가져오는 Reader
     * 영화와 TV 장르 목록을 가져와 중복을 제거한 후 반환.
     *
     * @return 장르 데이터를 반환하는 ItemReader
     */
    public ItemReader<List<GenreDto>> genreReader() {
        List<GenreDto> genreList = tmdbBatchComponent.fetchGenreList();
        log.info("일괄 처리를 위해 로드된 {} 장르", genreList.size());

        // 빈 리스트가 아닌 경우에만 리스트를 포함하는 단일 요소 리스트 생성
        List<List<GenreDto>> wrappedList = new ArrayList<>();
        if (!genreList.isEmpty()) {
            wrappedList.add(genreList);
        }

        return new ListItemReader<>(wrappedList);
    }

    /**
     * OTT 플랫폼을 가져오는 Reader
     * 영화와 TV OTT 플랫폼 목록을 가져와 중복을 제거한 후 반환.
     *
     * @return OTT 플랫폼 데이터를 반환하는 ItemReader
     */
    public ItemReader<List<OttWrapperDto>> ottReader() {
        List<OttWrapperDto> ottList = tmdbBatchComponent.fetchOttList();
        log.info("일괄 처리를 위한 {} OTT 플랫폼 로드됨", ottList.size());

        // 빈 리스트가 아닌 경우에만 리스트를 포함하는 단일 요소 리스트 생성
        List<List<OttWrapperDto>> wrappedList = new ArrayList<>();
        if (!ottList.isEmpty()) {
            wrappedList.add(ottList);
        }

        return new ListItemReader<>(wrappedList);
    }

    @Bean
    @StepScope
    @Lazy
    public ItemReader<List<MovieWatchProvidersDto>> movieWatchProvidersReader() {
        // 매번 새로운 Reader 인스턴스 생성
        return createReader();
    }

    /**
     * 영화 OTT 제공자 정보를 병렬로 읽어오는 Reader
     * TmdbMovieDetail 엔티티를 배치로 가져와서 각 영화의 OTT 제공자 정보를 병렬로 조회.
     *
     * @return 영화 OTT 제공자 정보를 반환하는 ItemReader
     */
    public ItemReader<List<MovieWatchProvidersDto>> createReader() {
        // 총 영화 수 확인
        long totalMovies = tmdbMovieDetailRepository.count();
        log.info("총 영화 수: {}", totalMovies);

        final int totalBatches = (int) Math.ceil(totalMovies / (double) BATCH_SIZE);

        final AtomicInteger currentBatch = new AtomicInteger(0);

        return new ItemReader<List<MovieWatchProvidersDto>>() {
            private boolean isCompleted = false;
            private ExecutorService executor = null;

            @Override
            public List<MovieWatchProvidersDto> read() {
                if (isCompleted) {
                    return null;
                }

                // 현재 배치 번호 가져오기
                int batchIndex = currentBatch.getAndIncrement();

                // 모든 배치를 처리했으면 종료
                if (batchIndex >= totalBatches) {
                    log.info("모든 {} 배치 처리 완료됨.", totalBatches);
                    if (executor != null) {
                        executor.shutdown(); // 모든 작업이 완료되면 스레드 풀 종료
                    }
                    isCompleted = true;
                    return null;
                }

                // 배치 사이에 잠시 대기하여 API 레이트 리밋 준수 (첫 배치는 제외)
                if (batchIndex > 0) {
                    try {
                        log.info("API 속도 제한을 준수하기 위해 배치 사이에 10초간 대기");
                        Thread.sleep(10000); // 10초 대기
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("배치 사이 대기 중 중단됨", e);
                    }
                }

                // 현재 배치의 영화 범위 계산
                int startIndex = batchIndex * BATCH_SIZE;
                int endIndex = Math.min(startIndex + BATCH_SIZE, (int) totalMovies);

                log.info("{}의 {} 배치 처리 중(영화 {}-{})",
                        batchIndex + 1, totalBatches, startIndex, endIndex - 1);

                // 현재 배치의 영화 목록 가져오기
                Pageable pageable = PageRequest.of(startIndex / PAGE_SIZE, PAGE_SIZE);
                List<TmdbMovieDetail> batchMovies = new ArrayList<>();

                // 현재 배치에 필요한 페이지들을 순차적으로 로드
                int remainingMovies = endIndex - startIndex;
                int currentPageIndex = startIndex / PAGE_SIZE;

                while (remainingMovies > 0) {
                    pageable = PageRequest.of(currentPageIndex, PAGE_SIZE);
                    Page<TmdbMovieDetail> moviePage = tmdbMovieDetailRepository.findAll(pageable);
                    List<TmdbMovieDetail> pageMovies = moviePage.getContent();

                    if (pageMovies.isEmpty()) {
                        log.warn("{} page에서 영화 찾을수없음", currentPageIndex);
                        currentPageIndex++;
                        continue;
                    }

                    // 첫 페이지에서는 시작 인덱스에 해당하는 영화부터 추가
                    if (currentPageIndex == startIndex / PAGE_SIZE) {
                        int startOffset = startIndex % PAGE_SIZE;
                        int count = Math.min(pageMovies.size() - startOffset, remainingMovies);
                        for (int i = startOffset; i < startOffset + count; i++) {
                            batchMovies.add(pageMovies.get(i));
                        }
                        remainingMovies -= count;
                    } else {
                        // 이후 페이지에서는 필요한 만큼만 추가
                        int count = Math.min(pageMovies.size(), remainingMovies);
                        for (int i = 0; i < count; i++) {
                            batchMovies.add(pageMovies.get(i));
                        }
                        remainingMovies -= count;
                    }

                    currentPageIndex++;

                    // 다음 페이지가 필요한 경우 짧은 지연 추가
                    if (remainingMovies > 0) {
                        try {
                            Thread.sleep(100); // 100ms 대기
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            log.error("페이지 로드 사이에 대기하는 동안 중단됨", e);
                        }
                    }
                }

                log.info("배치 {}에 대한 {}개의 영화를 로드", batchMovies.size(), batchIndex + 1);

                if (batchMovies.isEmpty()) {
                    log.warn("{} 배치에 Movie 없음", batchIndex + 1);
                    return read(); // 다음 배치 시도
                }

                executor = Executors.newFixedThreadPool(THREAD_COUNT);

                List<CompletableFuture<MovieWatchProvidersResult>> futures = new ArrayList<>();

                for (TmdbMovieDetail movie : batchMovies) {
                    CompletableFuture<MovieWatchProvidersResult> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            Set<WatchProvidersDto.ProviderInner> providers = tmdbMovieApiComponent.getMovieWatchProviders(movie.getTmdbId());
                            if (providers == null || providers.isEmpty()) {
                                return new MovieWatchProvidersResult(movie.getTmdbId(), null);
                            } else {
                                return new MovieWatchProvidersResult(movie.getTmdbId(), 
                                    new MovieWatchProvidersDto(movie.getId(), movie.getTmdbId(), providers));
                            }
                        } catch (Exception e) {
                            log.error("영화 ID {}에 대한 시청 제공자를 로드하는 중 오류 발생: {}}", movie.getTmdbId(), e.getMessage(), e);
                            return new MovieWatchProvidersResult(movie.getTmdbId(), null);
                        }
                    }, executor);

                    futures.add(future);

                    // 각 API 호출 사이에 짧은 지연 추가 (레이트 리밋 준수)
                    try {
                        Thread.sleep(50); // 50ms 대기
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("API 호출 사이에 대기하는 동안 중단됨", e);
                    }
                }

                // 모든 Future 결과 수집하고 TMDB ID 순서대로 정렬
                List<MovieWatchProvidersDto> batchResults = new ArrayList<>();
                try {
                    List<MovieWatchProvidersResult> results = new ArrayList<>();
                    for (CompletableFuture<MovieWatchProvidersResult> future : futures) {
                        results.add(future.get());
                    }

                    // TMDB ID 순서대로 정렬하여 순서 보장
                    results.sort(Comparator.comparingLong(MovieWatchProvidersResult::getTmdbId));

                    // 정렬된 순서대로 결과 추가 (null이 아닌 것만)
                    for (MovieWatchProvidersResult result : results) {
                        if (result.getData() != null) {
                            batchResults.add(result.getData());
                        }
                    }

                    log.info("영화 OTT 제공자 배치 {} 완료 - {}개 결과", batchIndex + 1, batchResults.size());

                } catch (Exception e) {
                    log.error("영화 OTT 제공자 배치 결과 수집 중 오류 발생: {}", e.getMessage(), e);
                    return new ArrayList<>();
                } finally {
                    if (executor != null) {
                        executor.shutdown();
                    }
                }

                if (batchResults.isEmpty()) {
                    return read();
                }

                return batchResults;
            }
        };
    }

    /**
     * 영화 이미지 정보를 병렬로 읽어오는 Reader
     * TmdbMovieDetail 엔티티를 배치로 가져와서 각 영화의 이미지 정보를 병렬로 조회.
     *
     * @return 영화 이미지 정보를 반환하는 ItemReader
     */
    public ItemReader<List<MovieImagesDto>> parallelMovieImagesReader() {
        // 총 영화 수 확인
        long totalMovies = tmdbMovieDetailRepository.count();
        log.info("총 영화 수: {}", totalMovies);

        // 총 배치 수 계산 - 각 배치는 BATCH_SIZE 개의 영화를 처리
        final int totalBatches = (int) Math.ceil(totalMovies / (double) BATCH_SIZE);

        log.info("Movie Image 읽기 시작. 총 영화: {}개, 총 배치: {}개, 스레드 수: {}개",
                totalMovies, totalBatches, THREAD_COUNT);

        // 현재 처리 중인 배치 번호
        final AtomicInteger currentBatch = new AtomicInteger(0);

        return new ItemReader<List<MovieImagesDto>>() {
            private boolean isCompleted = false;
            private ExecutorService executor = null;

            @Override
            public List<MovieImagesDto> read() {
                if (isCompleted) {
                    return null;
                }

                // 현재 배치 번호 가져오기
                int batchIndex = currentBatch.getAndIncrement();

                // 모든 배치를 처리했으면 종료
                if (batchIndex >= totalBatches) {
                    if (executor != null) {
                        executor.shutdown(); // 모든 작업이 완료되면 스레드 풀 종료
                    }
                    isCompleted = true;
                    return null;
                }

                // 배치 사이에 잠시 대기하여 API 레이트 리밋 준수 (첫 배치는 제외)
                if (batchIndex > 0) {
                    try {
                        Thread.sleep(10000); // 10초 대기
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("Interrupted while waiting between batches", e);
                    }
                }

                // 현재 배치의 영화 범위 계산
                int startIndex = batchIndex * BATCH_SIZE;
                int endIndex = Math.min(startIndex + BATCH_SIZE, (int) totalMovies);

                // 현재 배치의 영화 목록 가져오기
                Pageable pageable = PageRequest.of(startIndex / PAGE_SIZE, PAGE_SIZE);
                List<TmdbMovieDetail> batchMovies = new ArrayList<>();

                // 현재 배치에 필요한 페이지들을 순차적으로 로드
                int remainingMovies = endIndex - startIndex;
                int currentPageIndex = startIndex / PAGE_SIZE;

                while (remainingMovies > 0) {
                    pageable = PageRequest.of(currentPageIndex, PAGE_SIZE);
                    Page<TmdbMovieDetail> moviePage = tmdbMovieDetailRepository.findAll(pageable);
                    List<TmdbMovieDetail> pageMovies = moviePage.getContent();

                    if (pageMovies.isEmpty()) {
                        currentPageIndex++;
                        continue;
                    }

                    // 첫 페이지에서는 시작 인덱스에 해당하는 영화부터 추가
                    if (currentPageIndex == startIndex / PAGE_SIZE) {
                        int startOffset = startIndex % PAGE_SIZE;
                        int count = Math.min(pageMovies.size() - startOffset, remainingMovies);
                        for (int i = startOffset; i < startOffset + count; i++) {
                            batchMovies.add(pageMovies.get(i));
                        }
                        remainingMovies -= count;
                    } else {
                        // 이후 페이지에서는 필요한 만큼만 추가
                        int count = Math.min(pageMovies.size(), remainingMovies);
                        for (int i = 0; i < count; i++) {
                            batchMovies.add(pageMovies.get(i));
                        }
                        remainingMovies -= count;
                    }

                    currentPageIndex++;

                    // 다음 페이지가 필요한 경우 짧은 지연 추가
                    if (remainingMovies > 0) {
                        try {
                            Thread.sleep(100); // 100ms 대기
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            log.error("페이지 로드 사이에 대기하는 동안 중단됨", e);
                        }
                    }
                }

                if (batchMovies.isEmpty()) {
                    return read(); // 다음 배치 시도
                }

                executor = Executors.newFixedThreadPool(THREAD_COUNT);

                List<CompletableFuture<MovieImagesResult>> futures = new ArrayList<>();

                for (TmdbMovieDetail movie : batchMovies) {
                    CompletableFuture<MovieImagesResult> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            List<MovieImageWithTypeDto> images = tmdbBatchComponent.fetchMovieImages(movie.getTmdbId());

                            if (images == null || images.isEmpty()) {
                                return new MovieImagesResult(movie.getTmdbId(), 
                                    new MovieImagesDto(movie.getTmdbId(), new ArrayList<>()));
                            } else {
                                return new MovieImagesResult(movie.getTmdbId(),
                                    new MovieImagesDto(movie.getTmdbId(), images));
                            }
                        } catch (Exception e) {
                            log.error("영화 ID {}에 대한 이미지를 로드하는 중 오류 발생: {}", movie.getTmdbId(), e.getMessage(), e);
                            return new MovieImagesResult(movie.getTmdbId(),
                                new MovieImagesDto(movie.getTmdbId(), new ArrayList<>()));
                        }
                    }, executor);

                    futures.add(future);

                    // 각 API 호출 사이에 짧은 지연 추가 (레이트 리밋 준수)
                    try {
                        Thread.sleep(50); // 50ms 대기
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("API 호출 사이에 대기하는 동안 중단됨", e);
                    }
                }

                // 모든 Future 결과 수집하고 TMDB ID 순서대로 정렬
                List<MovieImagesDto> batchResults = new ArrayList<>();
                try {
                    List<MovieImagesResult> results = new ArrayList<>();
                    for (CompletableFuture<MovieImagesResult> future : futures) {
                        results.add(future.get());
                    }

                    // TMDB ID 순서대로 정렬하여 순서 보장
                    results.sort(Comparator.comparingLong(MovieImagesResult::getTmdbId));

                    // 정렬된 순서대로 결과 추가
                    for (MovieImagesResult result : results) {
                        batchResults.add(result.getData());
                    }

                    log.info("영화 이미지 배치 {} 완료 - {}개 결과", batchIndex + 1, batchResults.size());

                } catch (Exception e) {
                    log.error("영화 이미지 배치 결과 수집 중 오류 발생: {}", e.getMessage(), e);
                    return new ArrayList<>();
                } finally {
                    if (executor != null) {
                        executor.shutdown();
                    }
                }

                // 빈 배치는 다음 배치 시도
                if (batchResults.isEmpty()) {
                    return read(); // 다음 배치 시도
                }

                return batchResults;
            }
        };
    }

    /**
     * 영화 비디오 정보를 병렬로 읽어오는 Reader
     * TmdbMovieDetail 엔티티를 배치로 가져와서 각 영화의 비디오 정보를 병렬로 조회.
     *
     * @return 영화 비디오 정보를 반환하는 ItemReader
     */
    public ItemReader<List<MovieVideosDto>> parallelMovieVideosReader() {
        // 총 영화 수 확인
        long totalMovies = tmdbMovieDetailRepository.count();
        log.info("totalMovies: {}", totalMovies);

        // 총 배치 수 계산 - 각 배치는 BATCH_SIZE 개의 영화를 처리
        final int totalBatches = (int) Math.ceil(totalMovies / (double) BATCH_SIZE);

        log.info("Movie Video 읽기 시작. 총 영화: {}개, 총 배치: {}개, 스레드 수: {}개",
                totalMovies, totalBatches, THREAD_COUNT);

        // 현재 처리 중인 배치 번호
        final AtomicInteger currentBatch = new AtomicInteger(0);

        return new ItemReader<List<MovieVideosDto>>() {
            private boolean isCompleted = false;
            private ExecutorService executor = null;

            @Override
            public List<MovieVideosDto> read() {
                if (isCompleted) {
                    return null;
                }
                int batchIndex = currentBatch.getAndIncrement();

                if (batchIndex >= totalBatches) {
                    if (executor != null) {
                        executor.shutdown(); // 모든 작업이 완료되면 스레드 풀 종료
                    }
                    isCompleted = true;
                    return null;
                }

                // 배치 사이에 잠시 대기하여 API 레이트 리밋 준수 (첫 배치는 제외)
                if (batchIndex > 0) {
                    try {
                        log.info("API 속도 제한을 준수하기 위해 배치 사이에 10초간 대기");
                        Thread.sleep(10000); // 10초 대기
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("배치 사이 대기 중 중단됨", e);
                    }
                }

                // 현재 배치의 영화 범위 계산
                int startIndex = batchIndex * BATCH_SIZE;
                int endIndex = Math.min(startIndex + BATCH_SIZE, (int) totalMovies);

                Pageable pageable = PageRequest.of(startIndex / PAGE_SIZE, PAGE_SIZE);
                List<TmdbMovieDetail> batchMovies = new ArrayList<>();

                int remainingMovies = endIndex - startIndex;
                int currentPageIndex = startIndex / PAGE_SIZE;

                while (remainingMovies > 0) {
                    pageable = PageRequest.of(currentPageIndex, PAGE_SIZE);
                    Page<TmdbMovieDetail> moviePage = tmdbMovieDetailRepository.findAll(pageable);
                    List<TmdbMovieDetail> pageMovies = moviePage.getContent();

                    if (pageMovies.isEmpty()) {
                        log.warn("{} page 영화 찾을 수 없음", currentPageIndex);
                        currentPageIndex++;
                        continue;
                    }

                    // 첫 페이지에서는 시작 인덱스에 해당하는 영화부터 추가
                    if (currentPageIndex == startIndex / PAGE_SIZE) {
                        int startOffset = startIndex % PAGE_SIZE;
                        int count = Math.min(pageMovies.size() - startOffset, remainingMovies);
                        for (int i = startOffset; i < startOffset + count; i++) {
                            batchMovies.add(pageMovies.get(i));
                        }
                        remainingMovies -= count;
                    } else {
                        // 이후 페이지에서는 필요한 만큼만 추가
                        int count = Math.min(pageMovies.size(), remainingMovies);
                        for (int i = 0; i < count; i++) {
                            batchMovies.add(pageMovies.get(i));
                        }
                        remainingMovies -= count;
                    }

                    currentPageIndex++;

                    // 다음 페이지가 필요한 경우 짧은 지연 추가
                    if (remainingMovies > 0) {
                        try {
                            Thread.sleep(100); // 100ms 대기
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            log.error("페이지 로드 사이에 대기하는 동안 중단됨", e);
                        }
                    }
                }

                if (batchMovies.isEmpty()) {
                    return read(); // 다음 배치 시도
                }
                executor = Executors.newFixedThreadPool(THREAD_COUNT);

                List<CompletableFuture<MovieVideosResult>> futures = new ArrayList<>();

                for (TmdbMovieDetail movie : batchMovies) {
                    CompletableFuture<MovieVideosResult> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            List<VideoDto> videos = tmdbBatchComponent.fetchMovieVideos(movie.getTmdbId());

                            if (videos == null || videos.isEmpty()) {
                                return new MovieVideosResult(movie.getTmdbId(),
                                    new MovieVideosDto(movie.getTmdbId(), new ArrayList<>()));
                            } else {
                                return new MovieVideosResult(movie.getTmdbId(),
                                    new MovieVideosDto(movie.getTmdbId(), videos));
                            }
                        } catch (Exception e) {
                            log.error("영화 ID {}에 대한 비디오를 로드하는 중 오류 발생: {}\n", movie.getTmdbId(), e.getMessage(), e);
                            return new MovieVideosResult(movie.getTmdbId(),
                                new MovieVideosDto(movie.getTmdbId(), new ArrayList<>()));
                        }
                    }, executor);

                    futures.add(future);

                    // 각 API 호출 사이에 짧은 지연 추가 (레이트 리밋 준수)
                    try {
                        Thread.sleep(50); // 50ms 대기
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("API 호출 사이에 대기하는 동안 중단됨", e);
                    }
                }

                // 모든 Future 결과 수집하고 TMDB ID 순서대로 정렬
                List<MovieVideosDto> batchResults = new ArrayList<>();
                try {
                    List<MovieVideosResult> results = new ArrayList<>();
                    for (CompletableFuture<MovieVideosResult> future : futures) {
                        results.add(future.get());
                    }

                    // TMDB ID 순서대로 정렬하여 순서 보장
                    results.sort(Comparator.comparingLong(MovieVideosResult::getTmdbId));

                    // 정렬된 순서대로 결과 추가
                    for (MovieVideosResult result : results) {
                        batchResults.add(result.getData());
                    }

                    log.info("영화 비디오 배치 {} 완료 - {}개 결과", batchIndex + 1, batchResults.size());

                } catch (Exception e) {
                    log.error("영화 비디오 배치 결과 수집 중 오류 발생: {}", e.getMessage(), e);
                    return new ArrayList<>();
                } finally {
                    if (executor != null) {
                        executor.shutdown();
                    }
                }

                // 빈 배치는 다음 배치 시도
                if (batchResults.isEmpty()) {
                    return read(); // 다음 배치 시도
                }

                return batchResults;
            }
        };
    }

    /**
     * 데이터베이스에서 모든 TmdbMovieDetail의 tmdbId 목록을 가져옴.
     * @return 모든 TmdbMovieDetail의 tmdbId 목록
     */
    public List<Long> getAllTmdbMovieDetailTmdbIds() {
        log.info("Fetching all TmdbMovieDetail tmdbIds from database");
        List<TmdbMovieDetail> allMovies = tmdbMovieDetailRepository.findAll();
        List<Long> tmdbIds = allMovies.stream()
                .map(TmdbMovieDetail::getTmdbId)
                .collect(java.util.stream.Collectors.toList());
        log.info("Found {} tmdbIds in database", tmdbIds.size());
        return tmdbIds;
    }

    /**
     * 영화 크레딧 정보(배우, 제작진)를 병렬로 읽어오는 Reader
     * TmdbMovieDetail 엔티티를 배치로 가져와서 각 영화의 크레딧 정보를 병렬로 조회.
     * 배우와 제작진 정보를 중복 제거하여 TmdbMember 엔티티로 저장하고,
     * TmdbMovieCast와 TmdbMovieCrew 엔티티를 생성하여 TmdbMember와 TmdbMovieDetail 간의 관계를 설정.
     *
     * @return 영화 크레딧 정보를 반환하는 ItemReader
     */
    public ItemReader<List<CreditsWrapperDto>> parallelMovieCreditsReader() {
        // 총 영화 수 확인
        long totalMovies = tmdbMovieDetailRepository.count();
        log.info("총 영화 수: {}", totalMovies);

        // 총 배치 수 계산 - 각 배치는 BATCH_SIZE 개의 영화를 처리
        final int totalBatches = (int) Math.ceil(totalMovies / (double) BATCH_SIZE);

        log.info("Movie Credit 읽기 시작. 총 영화: {}개, 총 배치: {}개, 스레드 수: {}개",
                totalMovies, totalBatches, THREAD_COUNT);

        // 현재 처리 중인 배치 번호
        final AtomicInteger currentBatch = new AtomicInteger(0);

        return new ItemReader<List<CreditsWrapperDto>>() {
            private boolean isCompleted = false;
            private ExecutorService executor = null;

            @Override
            public List<CreditsWrapperDto> read() {
                if (isCompleted) {
                    return null;
                }
                int batchIndex = currentBatch.getAndIncrement();

                if (batchIndex >= totalBatches) {
                    if (executor != null) {
                        executor.shutdown(); // 모든 작업이 완료되면 스레드 풀 종료
                    }
                    isCompleted = true;
                    return null;
                }

                // 배치 사이에 잠시 대기하여 API 레이트 리밋 준수 (첫 배치는 제외)
                if (batchIndex > 0) {
                    try {
                        log.info("API 속도 제한을 준수하기 위해 배치 사이에 10초간 대기");
                        Thread.sleep(10000); // 10초 대기
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("배치 사이 대기 중 중단됨", e);
                    }
                }

                // 현재 배치의 영화 범위 계산
                int startIndex = batchIndex * BATCH_SIZE;
                int endIndex = Math.min(startIndex + BATCH_SIZE, (int) totalMovies);

                Pageable pageable = PageRequest.of(startIndex / PAGE_SIZE, PAGE_SIZE);
                List<TmdbMovieDetail> batchMovies = new ArrayList<>();

                int remainingMovies = endIndex - startIndex;
                int currentPageIndex = startIndex / PAGE_SIZE;

                while (remainingMovies > 0) {
                    pageable = PageRequest.of(currentPageIndex, PAGE_SIZE);
                    Page<TmdbMovieDetail> moviePage = tmdbMovieDetailRepository.findAll(pageable);
                    List<TmdbMovieDetail> pageMovies = moviePage.getContent();

                    if (pageMovies.isEmpty()) {
                        log.warn("{} page 영화 찾을 수 없음", currentPageIndex);
                        currentPageIndex++;
                        continue;
                    }

                    // 첫 페이지에서는 시작 인덱스에 해당하는 영화부터 추가
                    if (currentPageIndex == startIndex / PAGE_SIZE) {
                        int startOffset = startIndex % PAGE_SIZE;
                        int count = Math.min(pageMovies.size() - startOffset, remainingMovies);
                        for (int i = startOffset; i < startOffset + count; i++) {
                            batchMovies.add(pageMovies.get(i));
                        }
                        remainingMovies -= count;
                    } else {
                        // 이후 페이지에서는 필요한 만큼만 추가
                        int count = Math.min(pageMovies.size(), remainingMovies);
                        for (int i = 0; i < count; i++) {
                            batchMovies.add(pageMovies.get(i));
                        }
                        remainingMovies -= count;
                    }

                    currentPageIndex++;

                    // 다음 페이지가 필요한 경우 짧은 지연 추가
                    if (remainingMovies > 0) {
                        try {
                            Thread.sleep(50); // 50ms 대기
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            log.error("페이지 로드 사이에 대기하는 동안 중단됨", e);
                        }
                    }
                }

                if (batchMovies.isEmpty()) {
                    return read(); // 다음 배치 시도
                }
                executor = Executors.newFixedThreadPool(THREAD_COUNT);

                List<CompletableFuture<CreditsResult>> futures = new ArrayList<>();

                for (TmdbMovieDetail movie : batchMovies) {
                    CompletableFuture<CreditsResult> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            CreditsWrapperDto credits = tmdbBatchComponent.fetchMovieCredits(movie.getTmdbId());

                            if (credits == null) {
                                return new CreditsResult(movie.getTmdbId(),
                                    new CreditsWrapperDto(movie.getTmdbId().intValue(), new ArrayList<>(), new ArrayList<>()));
                            } else {
                                return new CreditsResult(movie.getTmdbId(), credits);
                            }
                        } catch (Exception e) {
                            log.error("영화 ID {}에 대한 크레딧을 로드하는 중 오류 발생: {}", movie.getTmdbId(), e.getMessage(), e);
                            return new CreditsResult(movie.getTmdbId(),
                                new CreditsWrapperDto(movie.getTmdbId().intValue(), new ArrayList<>(), new ArrayList<>()));
                        }
                    }, executor);

                    futures.add(future);

                    // 각 API 호출 사이에 짧은 지연 추가 (레이트 리밋 준수)
                    try {
                        Thread.sleep(50); // 50ms 대기
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("API 호출 사이에 대기하는 동안 중단됨", e);
                    }
                }

                // 모든 Future 결과 수집하고 TMDB ID 순서대로 정렬
                List<CreditsWrapperDto> batchResults = new ArrayList<>();
                try {
                    List<CreditsResult> results = new ArrayList<>();
                    for (CompletableFuture<CreditsResult> future : futures) {
                        results.add(future.get());
                    }

                    // TMDB ID 순서대로 정렬하여 순서 보장
                    results.sort(Comparator.comparingLong(CreditsResult::getTmdbId));

                    // 정렬된 순서대로 결과 추가
                    for (CreditsResult result : results) {
                        batchResults.add(result.getData());
                    }

                    log.info("영화 크레딧 배치 {} 완료 - {}개 결과", batchIndex + 1, batchResults.size());

                } catch (Exception e) {
                    log.error("영화 크레딧 배치 결과 수집 중 오류 발생: {}", e.getMessage(), e);
                    return new ArrayList<>();
                } finally {
                    if (executor != null) {
                        executor.shutdown();
                    }
                }

                // 중복 제거를 위한 멤버 집합
                Set<Long> uniqueMemberIds = new HashSet<>();
                Map<Long, TmdbMember> memberMap = new HashMap<>();

                // 모든 크레딧에서 멤버 정보 추출 및 중복 제거
                for (CreditsWrapperDto credits : batchResults) {
                    // 캐스트 멤버 처리
                    if (credits.getCast() != null) {
                        for (CastWrapperDto cast : credits.getCast()) {
                            if (cast.getId() != null && !uniqueMemberIds.contains(cast.getId().longValue())) {
                                uniqueMemberIds.add(cast.getId().longValue());

                                // 성별 변환
                                Gender gender = Gender.UNKNOWN;
                                if (cast.getGender() != null) {
                                    if (cast.getGender() == 1) {
                                        gender = Gender.MALE;
                                    } else if (cast.getGender() == 2) {
                                        gender = Gender.FEMALE;
                                    }
                                }

                                // TmdbMember 엔티티 생성
                                TmdbMember member = new TmdbMember(
                                    cast.getAdult(),
                                    cast.getId().longValue(),
                                    cast.getOriginalName(),
                                    cast.getName(),
                                    "movie",
                                    gender,
                                    cast.getProfilePath()
                                );

                                memberMap.put(cast.getId().longValue(), member);
                            }
                        }
                    }

                    // 크루 멤버 처리
                    if (credits.getCrew() != null) {
                        for (CrewWrapperDto crew : credits.getCrew()) {
                            if (crew.getId() != null && !uniqueMemberIds.contains(crew.getId().longValue())) {
                                uniqueMemberIds.add(crew.getId().longValue());

                                // 성별 변환
                                Gender gender = Gender.UNKNOWN;
                                if (crew.getGender() != null) {
                                    if (crew.getGender() == 1) {
                                        gender = Gender.MALE;
                                    } else if (crew.getGender() == 2) {
                                        gender = Gender.FEMALE;
                                    }
                                }

                                // TmdbMember 엔티티 생성
                                TmdbMember member = new TmdbMember(
                                    crew.getAdult(),
                                    crew.getId().longValue(),
                                    crew.getOriginalName(),
                                    crew.getName(),
                                    "movie",
                                    gender,
                                    crew.getProfilePath()
                                );

                                memberMap.put(crew.getId().longValue(), member);
                            }
                        }
                    }
                }

                // 로그 정보 출력
                log.info("중복 제거 후 {} 명의 멤버 정보 수집 완료", memberMap.size());
                log.info("{}개의 영화 크레딧 처리 완료 (배치 {})", batchResults.size(), batchIndex + 1);

                // 빈 배치는 다음 배치 시도
                if (batchResults.isEmpty()) {
                    return read();
                }

                return batchResults;
            }
        };
    }

    /**
     * 영화의 제작사 정보를 병렬로 읽어오는 Reader
     * TmdbMovieDetail 엔티티를 배치로 가져와서 각 영화의 상세 정보를 조회하여 제작사 정보를 추출.
     * 
     * @return 영화 제작사 정보를 반환하는 ItemReader
     */
    public ItemReader<List<MovieDetailWrapperDto>> parallelMovieDetailReader() {
        // 총 영화 수 확인
        long totalMovies = tmdbMovieDetailRepository.count();
        log.info("제작사 정보 추출을 위한 총 영화 수: {}", totalMovies);

        // 총 배치 수 계산 - 각 배치는 BATCH_SIZE 개의 영화를 처리
        final int totalBatches = (int) Math.ceil(totalMovies / (double) BATCH_SIZE);

        log.info("Movie Detail 읽기 시작. 총 영화: {}개, 총 배치: {}개, 스레드 수: {}개",
                totalMovies, totalBatches, THREAD_COUNT);

        // 현재 처리 중인 배치 번호
        final AtomicInteger currentBatch = new AtomicInteger(0);

        return new ItemReader<List<MovieDetailWrapperDto>>() {
            private boolean isCompleted = false;
            private ExecutorService executor = null;

            @Override
            public List<MovieDetailWrapperDto> read() {
                if (isCompleted) {
                    return null;
                }

                int batchIndex = currentBatch.getAndIncrement();

                if (batchIndex >= totalBatches) {
                    if (executor != null) {
                        executor.shutdown(); // 모든 작업이 완료되면 스레드 풀 종료
                    }
                    isCompleted = true;
                    return null;
                }

                // 배치 사이에 잠시 대기하여 API 레이트 리밋 준수 (첫 배치는 제외)
                if (batchIndex > 0) {
                    try {
                        log.info("API 속도 제한을 준수하기 위해 배치 사이에 10초간 대기");
                        Thread.sleep(10000); // 10초 대기
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("배치 사이 대기 중 중단됨", e);
                    }
                }

                // 현재 배치의 영화 범위 계산
                int startIndex = batchIndex * BATCH_SIZE;
                int endIndex = Math.min(startIndex + BATCH_SIZE, (int) totalMovies);

                log.info("배치 {} 처리 중 (영화 {}-{})", batchIndex + 1, startIndex, endIndex - 1);

                // 현재 배치의 영화 목록 가져오기
                Pageable pageable = PageRequest.of(startIndex / PAGE_SIZE, PAGE_SIZE);
                List<TmdbMovieDetail> batchMovies = new ArrayList<>();

                // 현재 배치에 필요한 페이지들을 순차적으로 로드
                int remainingMovies = endIndex - startIndex;
                int currentPageIndex = startIndex / PAGE_SIZE;

                while (remainingMovies > 0) {
                    pageable = PageRequest.of(currentPageIndex, PAGE_SIZE);
                    Page<TmdbMovieDetail> moviePage = tmdbMovieDetailRepository.findAll(pageable);
                    List<TmdbMovieDetail> pageMovies = moviePage.getContent();

                    if (pageMovies.isEmpty()) {
                        log.warn("{} page에서 영화를 찾을 수 없음", currentPageIndex);
                        currentPageIndex++;
                        continue;
                    }

                    // 첫 페이지에서는 시작 인덱스에 해당하는 영화부터 추가
                    if (currentPageIndex == startIndex / PAGE_SIZE) {
                        int startOffset = startIndex % PAGE_SIZE;
                        int count = Math.min(pageMovies.size() - startOffset, remainingMovies);
                        for (int i = startOffset; i < startOffset + count; i++) {
                            batchMovies.add(pageMovies.get(i));
                        }
                        remainingMovies -= count;
                    } else {
                        // 이후 페이지에서는 필요한 만큼만 추가
                        int count = Math.min(pageMovies.size(), remainingMovies);
                        for (int i = 0; i < count; i++) {
                            batchMovies.add(pageMovies.get(i));
                        }
                        remainingMovies -= count;
                    }

                    currentPageIndex++;

                    // 다음 페이지가 필요한 경우 짧은 지연 추가
                    if (remainingMovies > 0) {
                        try {
                            Thread.sleep(100); // 100ms 대기
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            log.error("페이지 로드 사이에 대기하는 동안 중단됨", e);
                        }
                    }
                }

                log.info("배치 {}에 대한 {}개의 영화를 로드", batchIndex + 1, batchMovies.size());

                if (batchMovies.isEmpty()) {
                    log.warn("{} 배치에 영화 없음", batchIndex + 1);
                    return read(); // 다음 배치 시도
                }

                executor = Executors.newFixedThreadPool(THREAD_COUNT);

                List<CompletableFuture<MovieDetailResult>> futures = new ArrayList<>();

                for (TmdbMovieDetail movie : batchMovies) {
                    CompletableFuture<MovieDetailResult> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            MovieDetailWrapperDto movieDetail = tmdbBatchComponent.fetchMovieDetail(movie.getTmdbId());

                            if (movieDetail == null) {
                                log.warn("영화 ID {}에 대한 상세 정보를 가져올 수 없음", movie.getTmdbId());
                                return new MovieDetailResult(movie.getTmdbId(), null);
                            }

                            return new MovieDetailResult(movie.getTmdbId(), movieDetail);
                        } catch (Exception e) {
                            log.error("영화 ID {}에 대한 상세 정보를 로드하는 중 오류 발생: {}", movie.getTmdbId(), e.getMessage(), e);
                            return new MovieDetailResult(movie.getTmdbId(), null);
                        }
                    }, executor);

                    futures.add(future);

                    // 각 API 호출 사이에 짧은 지연 추가 (레이트 리밋 준수)
                    try {
                        Thread.sleep(150); // 150ms 대기
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("API 호출 사이에 대기하는 동안 중단됨", e);
                    }
                }

                // 모든 Future 결과 수집하고 TMDB ID 순서대로 정렬
                List<MovieDetailWrapperDto> batchResults = new ArrayList<>();
                try {
                    List<MovieDetailResult> results = new ArrayList<>();
                    for (CompletableFuture<MovieDetailResult> future : futures) {
                        results.add(future.get());
                    }

                    // TMDB ID 순서대로 정렬하여 순서 보장
                    results.sort(Comparator.comparingLong(MovieDetailResult::getTmdbId));

                    // 정렬된 순서대로 결과 추가 (null이 아닌 것만)
                    for (MovieDetailResult result : results) {
                        if (result.getData() != null) {
                            batchResults.add(result.getData());
                        }
                    }

                    log.info("영화 상세 정보 배치 {} 완료 - {}개 결과", batchIndex + 1, batchResults.size());

                } catch (Exception e) {
                    log.error("영화 상세 정보 배치 결과 수집 중 오류 발생: {}", e.getMessage(), e);
                    return new ArrayList<>();
                } finally {
                    if (executor != null) {
                        executor.shutdown();
                    }
                }

                // 빈 배치는 다음 배치 시도
                if (batchResults.isEmpty()) {
                    return read(); // 다음 배치 시도
                }

                return batchResults;
            }
        };
    }

    /**
     * 영화-제작사 매핑을 위한 전용 Reader
     * 기존 영화들의 상세 정보를 조회하여 제작사 정보만 추출하여 매핑 관계 구성
     * 
     * @return 영화-제작사 매핑 정보를 반환하는 ItemReader
     */
    public ItemReader<List<CompanyMovieMappingDto>> parallelCompanyMovieMappingReader() {
        // 총 영화 수 확인
        long totalMovies = tmdbMovieDetailRepository.count();
        log.info("CompanyMovie 매핑을 위한 총 영화 수: {}", totalMovies);

        // 총 배치 수 계산 - 각 배치는 BATCH_SIZE 개의 영화를 처리
        final int totalBatches = (int) Math.ceil(totalMovies / (double) BATCH_SIZE);

        log.info("CompanyMovie 매핑 읽기 시작. 총 영화: {}개, 총 배치: {}개, 스레드 수: {}개",
                totalMovies, totalBatches, THREAD_COUNT);

        // 현재 처리 중인 배치 번호
        final AtomicInteger currentBatch = new AtomicInteger(0);

        return new ItemReader<List<CompanyMovieMappingDto>>() {
            private boolean isCompleted = false;
            private ExecutorService executor = null;

            @Override
            public List<CompanyMovieMappingDto> read() {
                if (isCompleted) {
                    return null;
                }

                int batchIndex = currentBatch.getAndIncrement();

                if (batchIndex >= totalBatches) {
                    if (executor != null) {
                        executor.shutdown(); // 모든 작업이 완료되면 스레드 풀 종료
                    }
                    isCompleted = true;
                    return null;
                }

                // 배치 사이에 잠시 대기하여 API 레이트 리밋 준수 (첫 배치는 제외)
                if (batchIndex > 0) {
                    try {
                        log.info("API 속도 제한을 준수하기 위해 배치 사이에 10초간 대기");
                        Thread.sleep(10000); // 10초 대기
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("배치 사이 대기 중 중단됨", e);
                    }
                }

                // 현재 배치의 영화 범위 계산
                int startIndex = batchIndex * BATCH_SIZE;
                int endIndex = Math.min(startIndex + BATCH_SIZE, (int) totalMovies);

                log.info("배치 {} 처리 중 (영화 {}-{})", batchIndex + 1, startIndex, endIndex - 1);

                // 현재 배치의 영화 목록 가져오기
                List<TmdbMovieDetail> batchMovies = getBatchMovies(startIndex, endIndex);

                if (batchMovies.isEmpty()) {
                    log.warn("{} 배치에 영화 없음", batchIndex + 1);
                    return read(); // 다음 배치 시도
                }

                executor = Executors.newFixedThreadPool(THREAD_COUNT);

                List<CompletableFuture<CompanyMovieMappingResult>> futures = new ArrayList<>();

                for (TmdbMovieDetail movie : batchMovies) {
                    CompletableFuture<CompanyMovieMappingResult> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            MovieDetailWrapperDto movieDetail = tmdbBatchComponent.fetchMovieDetail(movie.getTmdbId());

                            if (movieDetail == null || movieDetail.getProductionCompanies() == null || 
                                movieDetail.getProductionCompanies().isEmpty()) {
                                log.debug("영화 ID {}에 제작사 정보가 없음", movie.getTmdbId());
                                return new CompanyMovieMappingResult(movie.getTmdbId(), null);
                            }

                            // ProductionCompanyDto를 CompanyInfo로 변환
                            List<CompanyMovieMappingDto.CompanyInfo> companyInfos = movieDetail.getProductionCompanies().stream()
                                    .filter(company -> company.getId() != null)
                                    .map(company -> new CompanyMovieMappingDto.CompanyInfo(
                                            company.getId().longValue(),
                                            company.getName()
                                    ))
                                    .collect(Collectors.toList());

                            if (companyInfos.isEmpty()) {
                                return new CompanyMovieMappingResult(movie.getTmdbId(), null);
                            }

                            return new CompanyMovieMappingResult(movie.getTmdbId(),
                                new CompanyMovieMappingDto(
                                        movie.getTmdbId(),
                                        movie.getTitle(),
                                        companyInfos
                                ));
                        } catch (Exception e) {
                            log.error("영화 ID {} 제작사 매핑 정보 로드 중 오류 발생: {}", movie.getTmdbId(), e.getMessage());
                            return new CompanyMovieMappingResult(movie.getTmdbId(), null);
                        }
                    }, executor);

                    futures.add(future);

                    // 각 API 호출 사이에 짧은 지연 추가 (레이트 리밋 준수)
                    try {
                        Thread.sleep(150); // 150ms 대기
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("API 호출 사이에 대기하는 동안 중단됨", e);
                    }
                }

                // 모든 Future 결과 수집하고 TMDB ID 순서대로 정렬
                List<CompanyMovieMappingDto> batchResults = new ArrayList<>();
                try {
                    List<CompanyMovieMappingResult> results = new ArrayList<>();
                    for (CompletableFuture<CompanyMovieMappingResult> future : futures) {
                        results.add(future.get());
                    }

                    // TMDB ID 순서대로 정렬하여 순서 보장
                    results.sort(Comparator.comparingLong(CompanyMovieMappingResult::getTmdbId));

                    // 정렬된 순서대로 결과 추가 (null이 아닌 것만)
                    for (CompanyMovieMappingResult result : results) {
                        if (result.getData() != null) {
                            batchResults.add(result.getData());
                        }
                    }

                    log.info("영화-제작사 매핑 배치 {} 완료 - {}개 결과", batchIndex + 1, batchResults.size());

                } catch (Exception e) {
                    log.error("영화-제작사 매핑 배치 결과 수집 중 오류 발생: {}", e.getMessage(), e);
                    return new ArrayList<>();
                } finally {
                    if (executor != null) {
                        executor.shutdown();
                    }
                }

                // 빈 배치는 다음 배치 시도
                if (batchResults.isEmpty()) {
                    return read();
                }

                return batchResults;
            }
        };
    }

    /**
     * KOFIC 영화를 TMDB와 매핑하기 위한 Reader
     * KoficMovieDetail의 name을 사용하여 TMDB API에서 검색하고 매핑 가능한 영화를 찾습니다.
     * 
     * @return KOFIC-TMDB 매핑 정보를 반환하는 ItemReader
     */
    public ItemReader<List<KoficTmdbMappingDto>> koficTmdbMappingReader() {
        // KOFIC 영화 데이터베이스에서 모든 영화 가져오기 (TMDB 매핑이 안된 것만)
        List<KoficMovieDetail> unmappedKoficMovies = koficMovieDetailRepository.findAllByTmdbMovieDetailIsNull();

        if (unmappedKoficMovies.isEmpty()) {
            log.info("매핑되지 않은 KOFIC 영화가 없습니다.");
            return new ItemReader<List<KoficTmdbMappingDto>>() {
                @Override
                public List<KoficTmdbMappingDto> read() {
                    return null;
                }
            };
        }

        log.info("매핑되지 않은 KOFIC 영화 수: {}", unmappedKoficMovies.size());

        final int totalBatches = (int) Math.ceil(unmappedKoficMovies.size() / (double) BATCH_SIZE);
        final AtomicInteger currentBatch = new AtomicInteger(0);

        return new ItemReader<List<KoficTmdbMappingDto>>() {
            private boolean isCompleted = false;
            private ExecutorService executor = null;

            @Override
            public List<KoficTmdbMappingDto> read() {
                if (isCompleted) {
                    return null;
                }

                int batchIndex = currentBatch.getAndIncrement();

                if (batchIndex >= totalBatches) {
                    if (executor != null) {
                        executor.shutdown();
                    }
                    isCompleted = true;
                    return null;
                }

                // 배치 사이에 API 레이트 리밋 준수를 위한 대기
                if (batchIndex > 0) {
                    try {
                        log.info("TMDB API 속도 제한을 준수하기 위해 배치 사이에 10초간 대기");
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("배치 사이 대기 중 중단됨", e);
                    }
                }

                // 현재 배치의 KOFIC 영화 범위 계산
                int startIndex = batchIndex * BATCH_SIZE;
                int endIndex = Math.min(startIndex + BATCH_SIZE, unmappedKoficMovies.size());

                List<KoficMovieDetail> batchKoficMovies = unmappedKoficMovies.subList(startIndex, endIndex);

                log.info("KOFIC-TMDB 매핑 배치 {} 처리 중 (영화 {}-{})", 
                        batchIndex + 1, startIndex, endIndex - 1);

                executor = Executors.newFixedThreadPool(THREAD_COUNT);

                List<CompletableFuture<KoficTmdbMappingResult>> futures = new ArrayList<>();

                for (KoficMovieDetail koficMovie : batchKoficMovies) {
                    CompletableFuture<KoficTmdbMappingResult> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            // 1. 먼저 기존 DB에서 제목으로 검색
                    String movieName = koficMovie.getName();
                    Page<TmdbMovieDetail> existingMovies = tmdbMovieDetailRepository.findAllByTitleContainingOrderByPopularityDesc(
                            movieName, PageRequest.of(0, 5));

                            // 검색 결과가 있고, 첫 번째 결과의 제목이 KOFIC 영화 이름과 정확히 일치하는 경우
                    if (!existingMovies.isEmpty()) {
                        for (TmdbMovieDetail existingMovie : existingMovies.getContent()) {
                            // 제목이 정확히 일치하거나 매우 유사한 경우 (대소문자 무시)
                            if (existingMovie.getTitle().equalsIgnoreCase(movieName) || 
                                existingMovie.getTitle().replaceAll("\\s+", "").equalsIgnoreCase(movieName.replaceAll("\\s+", ""))) {
                                log.info("DB에서 제목 일치하는 영화 찾음 - KOFIC: {}, TMDB: {}", 
                                        movieName, existingMovie.getTitle());
                                        return new KoficTmdbMappingResult(koficMovie.getId(), 
                                        new KoficTmdbMappingDto(koficMovie, existingMovie, true));
                            }
                        }

                                // 정확히 일치하는 것은 없지만 유사한 결과가 있는 경우 로그
                                log.debug("DB에서 유사한 제목의 영화를 찾았으나 정확히 일치하지 않음 - KOFIC: {}, 유사 TMDB: {}", 
                                        movieName, existingMovies.getContent().get(0).getTitle());
                            }

                            // 2. DB에서 찾지 못한 경우 TMDB API로 검색
                            log.debug("DB에서 영화를 찾지 못해 TMDB API 검색 시도: {}", movieName);
                            SearchMovieWrapperDto searchResult = tmdbMovieApiComponent.getSearchMovieList(movieName, 1);

                            if (searchResult == null || searchResult.getResults() == null || searchResult.getResults().isEmpty()) {
                                log.debug("TMDB API에서도 영화를 찾을 수 없음: {}", movieName);
                                return new KoficTmdbMappingResult(koficMovie.getId(), null);
                            }

                                // 첫 번째 검색 결과를 선택 (가장 관련성이 높은 것으로 간주)
                                MovieResponseDto tmdbMovie = searchResult.getResults().get(0);

                                // 이미 데이터베이스에 존재하는지 확인
                                if (tmdbMovieDetailRepository.existsByTmdbId(tmdbMovie.getId().longValue())) {
                                log.debug("TMDB 영화가 이미 데이터베이스에 존재함 - KOFIC: {}, TMDB ID: {}", 
                                        movieName, tmdbMovie.getId());

                                // 기존 TmdbMovieDetail 조회하여 매핑만 수행
                                    Optional<TmdbMovieDetail> existingTmdbMovie = tmdbMovieDetailRepository.findByTmdbId(tmdbMovie.getId().longValue());
                                    if (existingTmdbMovie.isPresent()) {
                                    return new KoficTmdbMappingResult(koficMovie.getId(), 
                                                new KoficTmdbMappingDto(koficMovie, existingTmdbMovie.get(), true));
                                    }
                            }

                            // 새로운 TMDB 영화 데이터이므로 전체 데이터 수집 필요
                                    log.info("TMDB API에서 새 영화 데이터 찾음 - KOFIC: {}, TMDB: {}", 
                                            movieName, tmdbMovie.getTitle());
                            return new KoficTmdbMappingResult(koficMovie.getId(), 
                                            new KoficTmdbMappingDto(koficMovie, tmdbMovie, false));

                        } catch (Exception e) {
                            log.error("KOFIC 영화 {}에 대한 TMDB 매핑 중 오류 발생: {}", 
                                    koficMovie.getName(), e.getMessage(), e);
                            return new KoficTmdbMappingResult(koficMovie.getId(), null);
                        }
                    }, executor);

                    futures.add(future);

                        // API 호출 사이에 짧은 지연 추가 (레이트 리밋 준수)
                        try {
                        Thread.sleep(100); // 100ms 대기
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            log.error("API 호출 사이에 대기하는 동안 중단됨", e);
                        }
                    }

                // 모든 Future 결과 수집하고 KOFIC ID 순서대로 정렬
                List<KoficTmdbMappingDto> batchResults = new ArrayList<>();
                try {
                    List<KoficTmdbMappingResult> results = new ArrayList<>();
                    for (CompletableFuture<KoficTmdbMappingResult> future : futures) {
                        results.add(future.get());
                    }

                    // KOFIC ID 순서대로 정렬하여 순서 보장
                    results.sort(Comparator.comparingLong(KoficTmdbMappingResult::getKoficId));

                    // 정렬된 순서대로 결과 추가 (null이 아닌 것만)
                    for (KoficTmdbMappingResult result : results) {
                        if (result.getData() != null) {
                            batchResults.add(result.getData());
                        }
                    }

                    log.info("KOFIC-TMDB 매핑 배치 {} 완료 - {}개 결과", batchIndex + 1, batchResults.size());

                } catch (Exception e) {
                    log.error("KOFIC-TMDB 매핑 배치 결과 수집 중 오류 발생: {}", e.getMessage(), e);
                    return new ArrayList<>();
                } finally {
                    if (executor != null) {
                        executor.shutdown();
                    }
                }

                if (batchResults.isEmpty()) {
                    return read(); // 다음 배치 시도
                }

                return batchResults;
            }
        };
    }

    /**
     * 지정된 범위의 영화 목록을 가져오는 헬퍼 메서드
     */
    private List<TmdbMovieDetail> getBatchMovies(int startIndex, int endIndex) {
        List<TmdbMovieDetail> batchMovies = new ArrayList<>();
        int remainingMovies = endIndex - startIndex;
        int currentPageIndex = startIndex / PAGE_SIZE;

        while (remainingMovies > 0) {
            Pageable pageable = PageRequest.of(currentPageIndex, PAGE_SIZE);
            Page<TmdbMovieDetail> moviePage = tmdbMovieDetailRepository.findAll(pageable);
            List<TmdbMovieDetail> pageMovies = moviePage.getContent();

            if (pageMovies.isEmpty()) {
                log.warn("{} page에서 영화를 찾을 수 없음", currentPageIndex);
                currentPageIndex++;
                continue;
            }

            // 첫 페이지에서는 시작 인덱스에 해당하는 영화부터 추가
            if (currentPageIndex == startIndex / PAGE_SIZE) {
                int startOffset = startIndex % PAGE_SIZE;
                int count = Math.min(pageMovies.size() - startOffset, remainingMovies);
                for (int i = startOffset; i < startOffset + count; i++) {
                    batchMovies.add(pageMovies.get(i));
                }
                remainingMovies -= count;
            } else {
                // 이후 페이지에서는 필요한 만큼만 추가
                int count = Math.min(pageMovies.size(), remainingMovies);
                for (int i = 0; i < count; i++) {
                    batchMovies.add(pageMovies.get(i));
                }
                remainingMovies -= count;
            }

            currentPageIndex++;

            // 다음 페이지가 필요한 경우 짧은 지연 추가
            if (remainingMovies > 0) {
                try {
                    Thread.sleep(100); // 100ms 대기
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("페이지 로드 사이에 대기하는 동안 중단됨", e);
                }
            }
        }

        return batchMovies;
    }

    /**
     * 페이지 번호와 해당 페이지의 결과를 함께 저장하는 클래스
     * 멀티스레드 환경에서 순서 보장을 위해 사용
     */
    private static class PageResult {
        private final int pageNumber;
        private final List<MovieResponseDto> items;

        public PageResult(int pageNumber, List<MovieResponseDto> items) {
            this.pageNumber = pageNumber;
            this.items = items != null ? items : new ArrayList<>();
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public List<MovieResponseDto> getItems() {
            return items;
        }
    }

    /**
     * 영화 OTT 제공자 정보를 병렬로 읽어오는 Reader
     * TmdbMovieDetail 엔티티를 배치로 가져와서 각 영화의 OTT 제공자 정보를 병렬로 조회.
     * 결과를 순서대로 정렬하기 위해 별도의 Result 클래스를 사용합니다.
     */
    private static class MovieWatchProvidersResult {
        private final Long tmdbId;
        private final MovieWatchProvidersDto data;

        public MovieWatchProvidersResult(Long tmdbId, MovieWatchProvidersDto data) {
            this.tmdbId = tmdbId;
            this.data = data;
        }

        public Long getTmdbId() {
            return tmdbId;
        }

        public MovieWatchProvidersDto getData() {
            return data;
        }
    }

    /**
     * 영화 이미지 정보를 병렬로 읽어오는 Reader
     * TmdbMovieDetail 엔티티를 배치로 가져와서 각 영화의 이미지 정보를 병렬로 조회.
     * 결과를 순서대로 정렬하기 위해 별도의 Result 클래스를 사용합니다.
     */
    private static class MovieImagesResult {
        private final Long tmdbId;
        private final MovieImagesDto data;

        public MovieImagesResult(Long tmdbId, MovieImagesDto data) {
            this.tmdbId = tmdbId;
            this.data = data;
        }

        public Long getTmdbId() {
            return tmdbId;
        }

        public MovieImagesDto getData() {
            return data;
        }
    }

    /**
     * 영화 비디오 정보를 병렬로 읽어오는 Reader
     * TmdbMovieDetail 엔티티를 배치로 가져와서 각 영화의 비디오 정보를 병렬로 조회.
     * 결과를 순서대로 정렬하기 위해 별도의 Result 클래스를 사용합니다.
     */
    private static class MovieVideosResult {
        private final Long tmdbId;
        private final MovieVideosDto data;

        public MovieVideosResult(Long tmdbId, MovieVideosDto data) {
            this.tmdbId = tmdbId;
            this.data = data;
        }

        public Long getTmdbId() {
            return tmdbId;
        }

        public MovieVideosDto getData() {
            return data;
        }
    }

    /**
     * 영화 크레딧 정보를 병렬로 읽어오는 Reader
     * TmdbMovieDetail 엔티티를 배치로 가져와서 각 영화의 크레딧 정보를 병렬로 조회.
     * 결과를 순서대로 정렬하기 위해 별도의 Result 클래스를 사용합니다.
     */
    private static class CreditsResult {
        private final Long tmdbId;
        private final CreditsWrapperDto data;

        public CreditsResult(Long tmdbId, CreditsWrapperDto data) {
            this.tmdbId = tmdbId;
            this.data = data;
        }

        public Long getTmdbId() {
            return tmdbId;
        }

        public CreditsWrapperDto getData() {
            return data;
        }
    }

    /**
     * 영화 상세 정보를 병렬로 읽어오는 Reader
     * TmdbMovieDetail 엔티티를 배치로 가져와서 각 영화의 상세 정보를 병렬로 조회.
     * 결과를 순서대로 정렬하기 위해 별도의 Result 클래스를 사용합니다.
     */
    private static class MovieDetailResult {
        private final Long tmdbId;
        private final MovieDetailWrapperDto data;

        public MovieDetailResult(Long tmdbId, MovieDetailWrapperDto data) {
            this.tmdbId = tmdbId;
            this.data = data;
        }

        public Long getTmdbId() {
            return tmdbId;
        }

        public MovieDetailWrapperDto getData() {
            return data;
        }
    }

    /**
     * 영화-제작사 매핑을 위한 전용 Reader
     * 기존 영화들의 상세 정보를 조회하여 제작사 정보만 추출하여 매핑 관계 구성
     * 결과를 순서대로 정렬하기 위해 별도의 Result 클래스를 사용합니다.
     */
    private static class CompanyMovieMappingResult {
        private final Long tmdbId;
        private final CompanyMovieMappingDto data;

        public CompanyMovieMappingResult(Long tmdbId, CompanyMovieMappingDto data) {
            this.tmdbId = tmdbId;
            this.data = data;
        }

        public Long getTmdbId() {
            return tmdbId;
        }

        public CompanyMovieMappingDto getData() {
            return data;
        }
    }

    /**
     * KOFIC 영화를 TMDB와 매핑하기 위한 Reader
     * KoficMovieDetail의 name을 사용하여 TMDB API에서 검색하고 매핑 가능한 영화를 찾습니다.
     * 결과를 순서대로 정렬하기 위해 별도의 Result 클래스를 사용합니다.
     */
    private static class KoficTmdbMappingResult {
        private final Long koficId;
        private final KoficTmdbMappingDto data;

        public KoficTmdbMappingResult(Long koficId, KoficTmdbMappingDto data) {
            this.koficId = koficId;
            this.data = data;
        }

        public Long getKoficId() {
            return koficId;
        }

        public KoficTmdbMappingDto getData() {
            return data;
        }
    }

    /**
     * KOFIC에서 매핑된 TMDB 영화들의 상세 정보를 가져오는 Reader
     */
    public ItemReader<List<MovieDetailWrapperDto>> mappedMovieDetailReader() {
        // KOFIC과 매핑된 TMDB 영화 ID 목록 조회
        List<TmdbMovieDetail> mappedMovies = tmdbMovieDetailRepository.findAllByMovieIsNotNull();
        
        if (mappedMovies.isEmpty()) {
            log.info("매핑된 TMDB 영화가 없습니다.");
            return new ItemReader<List<MovieDetailWrapperDto>>() {
                @Override
                public List<MovieDetailWrapperDto> read() {
                    return null;
                }
            };
        }

        List<Long> tmdbIds = mappedMovies.stream()
                .map(TmdbMovieDetail::getTmdbId)
                .collect(Collectors.toList());

        log.info("매핑된 TMDB 영화 {}개에 대한 상세 정보 수집 시작", tmdbIds.size());

        return createMovieDetailBatchReader(tmdbIds);
    }

    /**
     * KOFIC에서 매핑된 TMDB 영화들의 장르 매칭을 처리하는 Reader
     */
    public ItemReader<List<Long>> mappedMovieGenreMatchReader() {
        List<TmdbMovieDetail> mappedMovies = tmdbMovieDetailRepository.findAllByMovieIsNotNull();
        
        if (mappedMovies.isEmpty()) {
            log.info("매핑된 TMDB 영화가 없어 장르 매칭을 건너뜁니다.");
            return new ItemReader<List<Long>>() {
                private boolean read = false;

                @Override
                public List<Long> read() {
                    if (read) {
                        return null;
                    }
                    read = true;
                    return new ArrayList<>();
                }
            };
        }

        List<Long> movieIds = mappedMovies.stream()
                .map(TmdbMovieDetail::getId)
                .collect(Collectors.toList());

        log.info("매핑된 TMDB 영화 {}개에 대한 장르 매칭 시작", movieIds.size());

        return new ItemReader<List<Long>>() {
            private boolean read = false;

            @Override
            public List<Long> read() {
                if (read) {
                    return null;
                }
                read = true;
                return movieIds;
            }
        };
    }

    /**
     * KOFIC에서 매핑된 영화-제작사 매핑을 처리하는 Reader
     */
    public ItemReader<List<CompanyMovieMappingDto>> mappedCompanyMovieMappingReader() {
        List<TmdbMovieDetail> mappedMovies = tmdbMovieDetailRepository.findAllByMovieIsNotNull();
        
        if (mappedMovies.isEmpty()) {
            log.info("매핑된 TMDB 영화가 없어 제작사 매핑을 건너뜁니다.");
            return new ItemReader<List<CompanyMovieMappingDto>>() {
                private boolean read = false;

                @Override
                public List<CompanyMovieMappingDto> read() {
                    if (read) {
                        return null;
                    }
                    read = true;
                    return new ArrayList<>();
                }
            };
        }

        List<CompanyMovieMappingDto> mappings = mappedMovies.stream()
                .filter(movie -> movie.getMovie() != null)
                .map(movie -> new CompanyMovieMappingDto(movie.getMovie().getId(), movie.getTmdbId()))
                .collect(Collectors.toList());

        log.info("매핑된 TMDB 영화 {}개에 대한 제작사 매핑 시작", mappings.size());

        return new ItemReader<List<CompanyMovieMappingDto>>() {
            private boolean read = false;

            @Override
            public List<CompanyMovieMappingDto> read() {
                if (read) {
                    return null;
                }
                read = true;
                return mappings;
            }
        };
    }

    /**
     * KOFIC에서 매핑된 TMDB 영화들의 OTT 제공자 정보를 가져오는 Reader
     */
    public ItemReader<List<MovieWatchProvidersDto>> mappedMovieWatchProvidersReader() {
        List<TmdbMovieDetail> mappedMovies = tmdbMovieDetailRepository.findAllByMovieIsNotNull();
        
        if (mappedMovies.isEmpty()) {
            log.info("매핑된 TMDB 영화가 없어 OTT 제공자 정보 수집을 건너뜁니다.");
            return new ItemReader<List<MovieWatchProvidersDto>>() {
                private boolean read = false;

                @Override
                public List<MovieWatchProvidersDto> read() {
                    if (read) {
                        return null;
                    }
                    read = true;
                    return new ArrayList<>();
                }
            };
        }

        List<MovieWatchProvidersDto> watchProviders = new ArrayList<>();
        
        log.info("매핑된 TMDB 영화 {}개에 대한 OTT 제공자 정보 수집 시작", mappedMovies.size());
        
        for (TmdbMovieDetail movie : mappedMovies) {
            try {
                Set<WatchProvidersDto.ProviderInner> providers = tmdbMovieApiComponent.getMovieWatchProviders(movie.getTmdbId());
                watchProviders.add(new MovieWatchProvidersDto(movie.getId(), movie.getTmdbId(), providers));
            } catch (Exception e) {
                log.error("영화 {}의 OTT 제공자 정보 조회 실패: {}", movie.getTmdbId(), e.getMessage());
            }
        }

        return new ItemReader<List<MovieWatchProvidersDto>>() {
            private boolean read = false;

            @Override
            public List<MovieWatchProvidersDto> read() {
                if (read) {
                    return null;
                }
                read = true;
                return watchProviders;
            }
        };
    }

    /**
     * KOFIC에서 매핑된 TMDB 영화들의 이미지 정보를 가져오는 Reader
     */
    public ItemReader<List<MovieImagesDto>> mappedMovieImagesReader() {
        List<TmdbMovieDetail> mappedMovies = tmdbMovieDetailRepository.findAllByMovieIsNotNull();
        
        if (mappedMovies.isEmpty()) {
            log.info("매핑된 TMDB 영화가 없어 이미지 정보 수집을 건너뜁니다.");
            return new ItemReader<List<MovieImagesDto>>() {
                private boolean read = false;

                @Override
                public List<MovieImagesDto> read() {
                    if (read) {
                        return null;
                    }
                    read = true;
                    return new ArrayList<>();
                }
            };
        }

        List<MovieImagesDto> movieImages = new ArrayList<>();
        
        log.info("매핑된 TMDB 영화 {}개에 대한 이미지 정보 수집 시작", mappedMovies.size());
        
        for (TmdbMovieDetail movie : mappedMovies) {
            try {
                List<MovieImageWithTypeDto> images = tmdbBatchComponent.fetchMovieImages(movie.getTmdbId());
                movieImages.add(new MovieImagesDto(movie.getId(), movie.getTmdbId(), images));
            } catch (Exception e) {
                log.error("영화 {}의 이미지 정보 조회 실패: {}", movie.getTmdbId(), e.getMessage());
            }
        }

        return new ItemReader<List<MovieImagesDto>>() {
            private boolean read = false;

            @Override
            public List<MovieImagesDto> read() {
                if (read) {
                    return null;
                }
                read = true;
                return movieImages;
            }
        };
    }

    /**
     * KOFIC에서 매핑된 TMDB 영화들의 비디오 정보를 가져오는 Reader
     */
    public ItemReader<List<MovieVideosDto>> mappedMovieVideosReader() {
        List<TmdbMovieDetail> mappedMovies = tmdbMovieDetailRepository.findAllByMovieIsNotNull();
        
        if (mappedMovies.isEmpty()) {
            log.info("매핑된 TMDB 영화가 없어 비디오 정보 수집을 건너뜁니다.");
            return new ItemReader<List<MovieVideosDto>>() {
                private boolean read = false;

                @Override
                public List<MovieVideosDto> read() {
                    if (read) {
                        return null;
                    }
                    read = true;
                    return new ArrayList<>();
                }
            };
        }

        List<MovieVideosDto> movieVideos = new ArrayList<>();
        
        log.info("매핑된 TMDB 영화 {}개에 대한 비디오 정보 수집 시작", mappedMovies.size());
        
        for (TmdbMovieDetail movie : mappedMovies) {
            try {
                List<VideoDto> videos = tmdbBatchComponent.fetchMovieVideos(movie.getTmdbId());
                movieVideos.add(new MovieVideosDto(movie.getId(), movie.getTmdbId(), videos));
            } catch (Exception e) {
                log.error("영화 {}의 비디오 정보 조회 실패: {}", movie.getTmdbId(), e.getMessage());
            }
        }

        return new ItemReader<List<MovieVideosDto>>() {
            private boolean read = false;

            @Override
            public List<MovieVideosDto> read() {
                if (read) {
                    return null;
                }
                read = true;
                return movieVideos;
            }
        };
    }

    /**
     * KOFIC에서 매핑된 TMDB 영화들의 크레딧 정보를 가져오는 Reader
     */
    public ItemReader<List<CreditsWrapperDto>> mappedMovieCreditsReader() {
        List<TmdbMovieDetail> mappedMovies = tmdbMovieDetailRepository.findAllByMovieIsNotNull();
        
        if (mappedMovies.isEmpty()) {
            log.info("매핑된 TMDB 영화가 없어 크레딧 정보 수집을 건너뜁니다.");
            return new ItemReader<List<CreditsWrapperDto>>() {
                private boolean read = false;

                @Override
                public List<CreditsWrapperDto> read() {
                    if (read) {
                        return null;
                    }
                    read = true;
                    return new ArrayList<>();
                }
            };
        }

        List<CreditsWrapperDto> movieCredits = new ArrayList<>();
        
        log.info("매핑된 TMDB 영화 {}개에 대한 크레딧 정보 수집 시작", mappedMovies.size());
        
        for (TmdbMovieDetail movie : mappedMovies) {
            try {
                CreditsWrapperDto credits = tmdbBatchComponent.fetchMovieCredits(movie.getTmdbId());
                if (credits != null) {
                    movieCredits.add(credits);
                }
            } catch (Exception e) {
                log.error("영화 {}의 크레딧 정보 조회 실패: {}", movie.getTmdbId(), e.getMessage());
            }
        }

        return new ItemReader<List<CreditsWrapperDto>>() {
            private boolean read = false;

            @Override
            public List<CreditsWrapperDto> read() {
                if (read) {
                    return null;
                }
                read = true;
                return movieCredits;
            }
        };
    }

    /**
     * 영화 상세 정보를 배치로 가져오는 공통 Reader 생성
     */
    private ItemReader<List<MovieDetailWrapperDto>> createMovieDetailBatchReader(List<Long> tmdbIds) {
        List<MovieDetailWrapperDto> movieDetails = new ArrayList<>();
        
        for (Long tmdbId : tmdbIds) {
            try {
                MovieDetailWrapperDto detail = tmdbBatchComponent.fetchMovieDetail(tmdbId);
                if (detail != null) {
                    movieDetails.add(detail);
                }
            } catch (Exception e) {
                log.error("영화 {}의 상세 정보 조회 실패: {}", tmdbId, e.getMessage());
            }
        }

        return new ItemReader<List<MovieDetailWrapperDto>>() {
            private boolean read = false;

            @Override
            public List<MovieDetailWrapperDto> read() {
                if (read) {
                    return null;
                }
                read = true;
                return movieDetails;
            }
        };
    }
}
