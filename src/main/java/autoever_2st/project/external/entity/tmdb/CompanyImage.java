package autoever_2st.project.external.entity.tmdb;

import autoever_2st.project.common.entity.TimeStamp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "company_image")
@Getter
@NoArgsConstructor
public class CompanyImage extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "ratio")
    private Double ratio;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "vote_average")
    private Double voteAverage;

    @Column(name = "vote_count")
    private Long voteCount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_company_id")
    private ProductCompany productCompany;

    public CompanyImage(String filePath, Double ratio, String fileType, Integer width, Integer height, Double voteAverage, Long voteCount) {
        this.filePath = filePath;
        this.ratio = ratio;
        this.fileType = fileType;
        this.width = width;
        this.height = height;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
    }

    public CompanyImage setProductCompany(ProductCompany productCompany){
        if(this.productCompany != null){
            throw new IllegalArgumentException("productCompany가 이미 존재합니다.");
        }
        this.productCompany = productCompany;
        return this;
    }

}
