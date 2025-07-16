package autoever_2st.project.external.entity.tmdb;

import autoever_2st.project.common.entity.TimeStamp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tmdb_job")
@NoArgsConstructor
@Getter
public class TmdbJob extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private TmdbDepartment department;

    public TmdbJob(String job) {
        this.job = job;
    }
    public TmdbJob setDepartment(TmdbDepartment department){
        this.department = department;
        department.addTmdbJob(this);
        return this;
    }
}
