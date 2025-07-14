package autoever_2st.project.external.entity.kofic;

import autoever_2st.project.common.entity.TimeStamp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "kofic_box_office")
@Getter
@NoArgsConstructor
public class KoficBoxOffice extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rank")
    private Integer rank;

    @Column(name = "customer_count")
    private Long customerCount;

    @Column(name = "cumulative_count")
    private Long cumulativeCount;

    @Column(name = "compared_by_yesterday")
    private Long comparedByYesterday;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kofic_movie_detail_id")
    private KoficMovieDetail koficMovieDetail;

    public KoficBoxOffice(Integer rank, Long customerCount, Long cumulativeCount, Long comparedByYesterday) {
        this.rank = rank;
        this.customerCount = customerCount;
        this.cumulativeCount = cumulativeCount;
        this.comparedByYesterday = comparedByYesterday;
    }

    public KoficBoxOffice setKoficMovieDetail(KoficMovieDetail koficMovieDetail){
        if(this.koficMovieDetail != null){
            throw new IllegalArgumentException("koficMovieDetail가 이미 존재합니다.");
        }
        this.koficMovieDetail = koficMovieDetail;
        koficMovieDetail.setKoficBoxOffice(this);
        return this;
    }
}
