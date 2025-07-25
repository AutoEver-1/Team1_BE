package autoever_2st.project.user.Entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "MemberInfo")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "MemberInfo_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "Member_id", unique = true, nullable = false)
    private Member member;

    private String user_agent;
    private String user_ip;
    private String user_locale;
    private String user_login;

}
