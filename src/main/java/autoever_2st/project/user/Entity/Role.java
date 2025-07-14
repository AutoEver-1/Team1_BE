package autoever_2st.project.user.Entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;

    @Enumerated(EnumType.STRING) // Enum의 이름을 문자열로 저장
    @Column(nullable = false, unique = true)
    private RoleType name;
}
