package autoever_2st.project.user.Service;

import autoever_2st.project.user.Entity.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private final Member member;
    public CustomUserDetails(Member member) {
        this.member = member;
    }


    public Member getMember() {
        return member;
    }

    // 현재 user의 role을 반환 (ex. "ROLE_ADMIN" / "ROLE_USER" 등)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(() -> member.getRole().getName().name()); // RoleType enum의 name을 가져오기 위해 .getName().name()
        //collection.add(() -> "ROLE_" + member.getRole().getName().name()); // RoleType enum의 name을 가져오기 위해 .getName().name()
        return collection;
    }



    // user의 비밀번호 반환
    @Override
    public String getPassword() {
        return member.getPassword();
    }

    // user의 username 반환
    @Override
    public String getUsername() {
        // Member에 username 필드가 없으므로, 로그인 아이디 역할 하는 email 반환
        return member.getEmail();
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}




