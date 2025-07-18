package autoever_2st.project.external.component.impl.tmdb;

import autoever_2st.project.external.component.TmdbApiComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public abstract class TmdbApiComponentImpl implements TmdbApiComponent {

    // TMDB API 제한: 10초당 40회
    private static final int API_RATE_LIMIT = 40; // 실제 제한인 40으로 변경
    private static final long RATE_LIMIT_WINDOW_MS = 10000; // 정확히 10초로 설정

    // 세마포어를 사용하여 동시 API 호출 수 제한
    private static final Semaphore API_SEMAPHORE = new Semaphore(API_RATE_LIMIT);

    // 원자적 카운터 사용 - Atomic, Instant
    private static final AtomicInteger tmdbApiCount = new AtomicInteger(0);
    private static volatile Instant lastResetTime = Instant.now();

    private static final ReentrantLock resetLock = new ReentrantLock();

    @Value("${api.tmdb.base-url}")
    private String tmdbBaseApiUrl;

    @Value("${api.tmdb.api-key}")
    private String tmdbApiKey;

    /**
     * API 호출 제한 관리 메서드
     * TMDB API는 10초당 40회로 제한되어 있음
     * 세마포어와 토큰 버킷 알고리즘을 사용하여 효율적으로 관리
     */
    protected  void checkRateLimit() {
        Instant now = Instant.now();
        long elapsedTime = now.toEpochMilli() - lastResetTime.toEpochMilli();

        // 10초가 지났는지 확인하고 카운터 리셋
        if (elapsedTime >= RATE_LIMIT_WINDOW_MS) {
            if (resetLock.tryLock()) {
                try {
                    // 다른 스레드가 이미 리셋했는지 다시 확인
                    elapsedTime = Instant.now().toEpochMilli() - lastResetTime.toEpochMilli();
                    if (elapsedTime >= RATE_LIMIT_WINDOW_MS) {
                        int currentCount = tmdbApiCount.getAndSet(0);
                        lastResetTime = Instant.now();

                        // 세마포어 리셋 (사용 가능한 퍼밋을 최대로 복원)
                        int permitsToRelease = API_RATE_LIMIT - API_SEMAPHORE.availablePermits();
                        if (permitsToRelease > 0) {
                            API_SEMAPHORE.release(permitsToRelease);
                        }

                        log.debug("TMDB API 카운터 리셋. 이전 카운트: {}", currentCount);
                    }
                } finally {
                    resetLock.unlock();
                }
            }
        }

        try {
            // 세마포어 획득 시도 (최대 1초 대기)
            if (!API_SEMAPHORE.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
                log.info("TMDB API 호출 제한에 도달. 세마포어 대기");
                // 1초 후에도 획득 실패하면 무기한 대기 (블로킹)
                log.warn("TMDB API 세마포어 획득 대기");
                API_SEMAPHORE.acquire();
                log.info("TMDB API 세마포어 획득 성공");
            }

            // API 호출 카운터 증가
            int count = tmdbApiCount.incrementAndGet();
            if (count % 10 == 0) {
                log.info("TMDB API 호출 카운트: {}", count);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("API 호출 제한 대기 중 인터럽트 발생", e);
        }
    }

    protected RestClient getOriginRestClient() {
        checkRateLimit();
        return RestClient.builder()
                .baseUrl(tmdbBaseApiUrl)
                .build();
    }

    protected RestClient getMovieRestClient() {
        checkRateLimit();
        return RestClient.builder()
                .baseUrl(tmdbBaseApiUrl + "/movie")
                .build();
    }

    protected RestClient getPersonRestClient() {
        checkRateLimit();
        return RestClient.builder()
                .baseUrl(tmdbBaseApiUrl + "/person")
                .build();
    }

    protected RestClient getCollectionRestClient() {
        checkRateLimit();
        return RestClient.builder()
                .baseUrl(tmdbBaseApiUrl + "/collection")
                .build();
    }

    protected RestClient getCompanyRestClient() {
        checkRateLimit();
        return RestClient.builder()
                .baseUrl(tmdbBaseApiUrl + "/company")
                .build();
    }

    protected RestClient getConfigurationRestClient() {
        checkRateLimit();
        return RestClient.builder()
                .baseUrl(tmdbBaseApiUrl + "/configuration")
                .build();
    }

    protected RestClient getTrendRestClient() {
        checkRateLimit();
        return RestClient.builder()
                .baseUrl(tmdbBaseApiUrl + "/trending")
                .build();
    }

    protected RestClient getTvRestClient() {
        checkRateLimit();
        return RestClient.builder()
                .baseUrl(tmdbBaseApiUrl + "/tv")
                .build();
    }

    protected RestClient getWatchProviderRestClient() {
        checkRateLimit();
        return RestClient.builder()
                .baseUrl(tmdbBaseApiUrl + "/watch/providers")
                .build();
    }

    protected String getApiKey() {
        return this.tmdbApiKey;
    }
}
