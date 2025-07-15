package autoever_2st.project.user.Entity;


import jakarta.persistence.*;
import lombok.*;

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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    @JoinColumn(name = "role_id", unique = true)
    private Role role;

    @Column(nullable = false, unique = true)
    private String email;
    private String password;

    private String name;
    private String gender;
    private LocalDate birth_date;

    @Column(unique = true)
    private String nickname;
    private String profile_img_url;

    private Boolean is_delete;
    private Boolean is_banned;

}
