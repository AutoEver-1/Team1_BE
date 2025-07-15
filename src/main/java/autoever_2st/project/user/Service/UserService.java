package autoever_2st.project.user.Service;

import autoever_2st.project.user.Entity.Member;
import autoever_2st.project.user.Repository.UserRepository;
import autoever_2st.project.user.dto.request.SignupRequestDto;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    // 회원가입
    public void registerUser(SignupRequestDto signupRequestDto) {
        // 이메일 중복 검증 -> 이미 가입한 유저인지 확인
        if (userRepository.existsByEmail(signupRequestDto.getEmail())) {
            throw new RuntimeException("회원가입 실패 : 이미 가입한 유저입니다.");
        }

        // Member 엔티티 생성
        Member member = Member.builder()
                .email(signupRequestDto.getEmail())
                .password(bCryptPasswordEncoder.encode(signupRequestDto.getPassword()))
                .name(signupRequestDto.getName())
                .gender(signupRequestDto.getGender())
                .birth_date(LocalDate.parse(signupRequestDto.getBirth_date()))
                .build();
        userRepository.save(member);
    }
}
