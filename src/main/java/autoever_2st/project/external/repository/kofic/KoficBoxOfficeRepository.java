package autoever_2st.project.external.repository.kofic;

import autoever_2st.project.external.entity.kofic.KoficBoxOffice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * KOFIC 박스오피스 데이터 저장소
 */
@Repository
public interface KoficBoxOfficeRepository extends JpaRepository<KoficBoxOffice, Long> {
}