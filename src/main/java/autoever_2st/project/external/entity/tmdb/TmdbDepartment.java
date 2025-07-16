package autoever_2st.project.external.entity.tmdb;

import autoever_2st.project.common.entity.TimeStamp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tmdb_department")
@Getter
@NoArgsConstructor
public class TmdbDepartment extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "department")
    private String department;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TmdbJob> tmdbJob = new ArrayList<>();

    public TmdbDepartment(String department) {
        this.department = department;
    }

    public TmdbDepartment addTmdbJob(TmdbJob tmdbJob){
        this.tmdbJob.add(tmdbJob);
        tmdbJob.setDepartment(this);
        return this;
    }

    public TmdbDepartment removeTmdbJob(TmdbJob tmdbJob){
        this.tmdbJob.remove(tmdbJob);
        return this;
    }

}
