package autoever_2st.project.user.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Builder
@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(nullable = false)
    private String email;
    private String password;

    private String name;
    private String gender;
    private LocalDate birth_date;

    private String nickname;
    @ColumnDefault("1")         // UserService에서 기본 url asset 경로로 수정
    private String profile_img_url;
    @ColumnDefault("false")
    private Boolean is_delete;
    @ColumnDefault("false")
    private Boolean is_banned;

}
