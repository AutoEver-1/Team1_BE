package autoever_2st.project.external.entity.tmdb;

import autoever_2st.project.common.entity.TimeStamp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "alternative_company_name")
@Getter
@NoArgsConstructor
public class AlternativeCompanyName extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_company_id")
    private ProductCompany productCompany;

    public AlternativeCompanyName(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public AlternativeCompanyName setProductCompany(ProductCompany productCompany){
        this.productCompany = productCompany;
        productCompany.addAlternativeCompanyName(this);
        return this;
    }

}
