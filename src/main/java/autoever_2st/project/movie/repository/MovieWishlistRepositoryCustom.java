package autoever_2st.project.movie.repository;

import autoever_2st.project.reviewer.dto.WishlistItemDto;

import java.util.List;
import java.util.Map;

public interface MovieWishlistRepositoryCustom {
    
    /**
     * 여러 멤버의 위시리스트를 한 번에 조회 (N+1 최적화)
     * @param memberIds 조회할 멤버 ID 리스트
     * @param limit 각 멤버당 최대 조회할 위시리스트 개수
     * @return 멤버 ID를 키로 하는 WishlistItemDto 리스트 맵
     */
    Map<Long, List<WishlistItemDto>> findWishlistItemsByMemberIds(List<Long> memberIds, int limit);
} 