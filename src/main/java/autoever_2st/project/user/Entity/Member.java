package autoever_2st.project.user.Entity;


import autoever_2st.project.common.entity.TimeStamp;
import autoever_2st.project.user.Entity.Follow.MemberFollower;
import autoever_2st.project.user.Entity.Follow.MemberFollowing;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Builder
@Entity
@Table(name = "member")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member extends TimeStamp {
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
    private Date birth_date;

    private String nickname;
    @ColumnDefault("1")         // UserService에서 기본 url asset 경로로 수정
    private String profile_img_url;
    @ColumnDefault("false")
    private Boolean is_delete;
    @ColumnDefault("false")
    private Boolean is_banned;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private JwtToken jwtToken;

    public String getProfileImgUrl() {
        return profile_img_url;
    }

    // 나를 팔로우한 사람들
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberFollower> followers = new ArrayList<>();

    // 내가 팔로우하는 사람들
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberFollowing> followings = new ArrayList<>();


}
