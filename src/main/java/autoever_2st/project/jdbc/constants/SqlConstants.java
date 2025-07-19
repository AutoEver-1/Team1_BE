package autoever_2st.project.jdbc.constants;

/**
 * SQL 쿼리 상수 클래스
 * 
 * 애플리케이션에서 사용되는 모든 SQL 쿼리를 중앙 집중화하여 관리.
 */
public class SqlConstants {

    // TmdbMovieDetail 관련 쿼리
    public static final String FIND_EXISTING_MOVIE_DETAILS = 
            "SELECT id, tmdb_id, title, updated_at FROM tmdb_movie_detail " +
            "WHERE tmdb_id IN (:tmdbIds)";

    public static final String INSERT_MOVIE_DETAIL = 
            "INSERT INTO tmdb_movie_detail (is_adult, tmdb_id, title, original_title, " +
            "original_language, overview, status, release_date, runtime, video, " +
            "vote_average, vote_count, popularity, media_type, registed_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String UPDATE_MOVIE_DETAIL = 
            "UPDATE tmdb_movie_detail SET title = ?, original_title = ?, overview = ?, " +
            "vote_average = ?, vote_count = ?, popularity = ?, updated_at = ? " +
            "WHERE id = ?";

    // MovieGenre 관련 쿼리
    public static final String FIND_EXISTING_GENRES = 
            "SELECT id, genre_id, name FROM movie_genre " +
            "WHERE genre_id IN (:genreIds)";

    public static final String FIND_ALL_GENRES = 
            "SELECT id, genre_id, name FROM movie_genre";

    public static final String INSERT_GENRE = 
            "INSERT INTO movie_genre (genre_id, name, registed_at, updated_at) " +
            "VALUES (?, ?, ?, ?)";

    public static final String UPDATE_GENRE = 
            "UPDATE movie_genre SET name = ?, updated_at = ? " +
            "WHERE id = ?";

    // MovieGenreMatch 관련 쿼리
    public static final String INSERT_GENRE_MATCH = 
            "INSERT INTO movie_genre_match (tmdb_movie_detail_id, movie_genre_id, registed_at, updated_at) " +
            "VALUES (?, ?, ?, ?)";

    // Movie 관련 쿼리
    public static final String FIND_EXISTING_MOVIES_BY_TMDB_ID = 
            "SELECT id, tmdb_movie_detail_id FROM movie " +
            "WHERE tmdb_movie_detail_id IN (:tmdbMovieDetailIds)";

    public static final String INSERT_MOVIE = 
            "INSERT INTO movie (tmdb_movie_detail_id, registed_at, updated_at) " +
            "VALUES (?, ?, ?)";

    public static final String UPDATE_MOVIE = 
            "UPDATE movie SET updated_at = ? " +
            "WHERE id = ?";

    // OttPlatform 관련 쿼리
    public static final String FIND_EXISTING_OTT_PLATFORMS = 
            "SELECT id, tmdb_ott_id, name FROM ott_platform " +
            "WHERE tmdb_ott_id IN (:tmdbOttIds)";

    public static final String FIND_ALL_OTT_PLATFORMS = 
            "SELECT id, tmdb_ott_id, name FROM ott_platform";

    public static final String INSERT_OTT_PLATFORM = 
            "INSERT INTO ott_platform (tmdb_ott_id, name, registed_at, updated_at) " +
            "VALUES (?, ?, ?, ?)";

    public static final String UPDATE_OTT_PLATFORM = 
            "UPDATE ott_platform SET name = ?, updated_at = ? " +
            "WHERE id = ?";

    // TmdbMovieDetailOtt 관련 쿼리
    public static final String INSERT_MOVIE_DETAIL_OTT = 
            "INSERT INTO tmdb_movie_detail_ott (tmdb_movie_detail_id, ott_platform_id, registed_at, updated_at) " +
            "VALUES (?, ?, ?, ?)";

    // TmdbMovieImages 관련 쿼리
    public static final String INSERT_MOVIE_IMAGES = 
            "INSERT INTO tmdb_movie_images (image_url, base_url, width, height, ratio, image_type, iso_639_1, tmdb_movie_detail_id, registed_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    // TmdbMovieVideo 관련 쿼리
    public static final String INSERT_MOVIE_VIDEO = 
            "INSERT INTO tmdb_movie_video (video_url, base_url, name, site, video_type, iso_639_1, tmdb_movie_detail_id, registed_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    // TmdbMember 관련 쿼리
    public static final String FIND_EXISTING_MEMBERS = 
            "SELECT id, tmdb_id, name, original_name, gender, profile_path, updated_at FROM tmdb_member " +
            "WHERE tmdb_id IN (:tmdbIds)";

    public static final String FIND_ALL_MEMBERS = 
            "SELECT id, tmdb_id, name, original_name, gender, profile_path, updated_at FROM tmdb_member";

    public static final String INSERT_MEMBER = 
            "INSERT INTO tmdb_member (is_adult, tmdb_id, original_name, name, media_type, gender, profile_path, registed_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE name = VALUES(name), original_name = VALUES(original_name), " +
            "profile_path = VALUES(profile_path), updated_at = VALUES(updated_at)";

    public static final String UPDATE_MEMBER = 
            "UPDATE tmdb_member SET name = ?, original_name = ?, profile_path = ?, updated_at = ? " +
            "WHERE id = ?";

    // TmdbMovieCast 관련 쿼리
    public static final String INSERT_MOVIE_CAST = 
            "INSERT INTO tmdb_movie_cast (cast_character, cast_order, tmdb_cast_id, known_for_department, tmdb_movie_detail_id, tmdb_member_id, registed_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    // TmdbMovieCrew 관련 쿼리
    public static final String INSERT_MOVIE_CREW = 
            "INSERT INTO tmdb_movie_crew (tmdb_credit_id, department, job, tmdb_movie_detail_id, tmdb_member_id, registed_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";
}
