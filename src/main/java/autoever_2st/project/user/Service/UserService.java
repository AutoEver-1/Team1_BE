package autoever_2st.project.user.Service;

import autoever_2st.project.user.Entity.JwtToken;
import autoever_2st.project.user.Entity.Member;
import autoever_2st.project.user.Entity.Role;
import autoever_2st.project.user.Entity.RoleType;
import autoever_2st.project.user.Repository.JwtTokenRepository;
import autoever_2st.project.user.Repository.RoleRepository;
import autoever_2st.project.user.Repository.UserRepository;
import autoever_2st.project.user.dto.request.LoginRequestDto;
import autoever_2st.project.user.dto.request.SignupRequestDto;
import autoever_2st.project.user.dto.response.LoginResponseDto;
import autoever_2st.project.user.jwt.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RoleRepository roleRepository;
    private final JWTUtil jwtUtil;
    private final JwtTokenRepository jwtTokenRepository;


    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, RoleRepository roleRepository, JWTUtil jwtUtil, JwtTokenRepository jwtTokenRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
        this.jwtTokenRepository = jwtTokenRepository;
    }


    // 닉네임 발생기
    private static final String[] KOREAN_ADJECTIVES = {
            "방실", "말랑", "쫀득", "복슬", "보들", "졸린", "싱글", "반짝", "동글", "몽실",
            "소복", "몽글", "새근", "토실", "말똥", "느긋", "포근", "보송", "차분", "아늑",
            "살랑", "사뿐", "살포시", "재잘", "뽀짝", "수줍", "따끈", "달달", "귀염", "앙증",
            "싱긋", "쫑긋", "찰랑", "꼬물", "말랑이", "사르르", "따박", "콩닥", "느릿", "몽환",
            "도도", "몽환적인", "샤방", "반짝반짝", "순둥", "몽이", "몽자", "토글토글", "비틀비틀", "구름"
    };

    private static final String[] KOREAN_NOUNS = {
            "토끼", "곰돌이", "냥냥이", "멍멍이", "다람쥐", "콩떡", "푸딩", "햄찌", "치치", "복숭아",
            "쫀득이", "젤리", "솜사탕", "마카롱", "호빵", "방울이", "우주쥐", "오리", "햄토리", "도치",
            "우산", "우유", "달고나", "꼬깔콘", "솜뭉치", "파인애플", "피카츄", "뚜뚜", "몽이", "몽자",
            "구르미", "두더지", "이불", "쿠션", "펭귄", "앵무", "몽돌", "소라", "구름빵", "빵빵이",
            "낙타", "나뭇잎", "단무지", "모찌", "달토끼", "버섯", "미역", "문어", "오징어", "라면"
    };

    private String generateUniqueKoreanNickname() {
        String nickname;
        do {
            String adjective = KOREAN_ADJECTIVES[(int) (Math.random() * KOREAN_ADJECTIVES.length)];
            String noun = KOREAN_NOUNS[(int) (Math.random() * KOREAN_NOUNS.length)];
            int number = (int) (Math.random() * 900 + 100); // 100~999
            nickname = adjective + noun + number;
        } while (userRepository.existsByNickname(nickname));
        return nickname;
    }

    // 회원가입
    public void signup(SignupRequestDto signupRequestDto) {
        // 이메일 중복 검증 -> 이미 가입한 유저인지 확인
        if (userRepository.existsByEmail(signupRequestDto.getEmail())) {
            throw new RuntimeException("회원가입 실패 : 이미 가입한 유저입니다.");
        }

        // RoleRepository에서 USER 역할 Role 객체 조회
        Role userRole = roleRepository.findByName(RoleType.USER)
                .orElseThrow(() -> new RuntimeException("USER Role이 DB에 없음"));


        String generatedNickname = generateUniqueKoreanNickname();

        // Member 엔티티 생성
        Member member = Member.builder()
                .email(signupRequestDto.getEmail())
                .password(bCryptPasswordEncoder.encode(signupRequestDto.getPassword()))
                .name(signupRequestDto.getName())
                .gender(signupRequestDto.getGender())
                .birth_date(Date.from(LocalDate.parse(signupRequestDto.getBirth_date()).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .role(userRole)   // 여기서 기본값처럼 넣어줌
                .nickname(generatedNickname)
                .profile_img_url("1")
                .is_delete(false)
                .is_banned(false)
                .build();
        userRepository.save(member);
    }

    //로그인, 토큰 발행
    public LoginResponseDto loginAndIssueTokens(LoginRequestDto loginRequestDto) {
        Optional<Member> optionalMember = userRepository.findByEmail(loginRequestDto.getEmail());

        if (optionalMember.isEmpty()) {
            throw new RuntimeException("이메일이 존재하지 않습니다.");
        }

        Member member = optionalMember.get();
        if (!bCryptPasswordEncoder.matches(loginRequestDto.getPassword(), member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // JWT 발급
        String accessToken = jwtUtil.createJwt(member.getEmail(), member.getRole().getName().name(), 1000 * 60 * 60L);
        String refreshToken = jwtUtil.createJwt(member.getEmail(), member.getRole().getName().name(), 1000 * 60 * 60 * 24 * 7L);

        // DB에 저장
        JwtToken jwtToken = jwtTokenRepository.findByMember(member).orElse(new JwtToken());
        jwtToken.setAccessToken(accessToken);
        jwtToken.setRefreshToken(refreshToken);
        jwtToken.setMember(member);
        jwtTokenRepository.save(jwtToken);

        return LoginResponseDto.builder()
                .memberId(member.getId())
                .nickName(member.getNickname())
                .gender(member.getGender())
                .profilePath(member.getProfile_img_url())
                .roleName(member.getRole().getName().name())
                .realName(member.getName())
                .token(accessToken)
                .build();
    }


//    // 로그인 메서드 추가
//    public Member login(LoginRequestDto loginRequestDto) {
////        Member member = userRepository.findByEmail(loginRequestDto.getEmail());
////        if (member != null && bCryptPasswordEncoder.matches(loginRequestDto.getPassword(), member.getPassword())) {
////            return member;
////        }
//        //test
//        log.info("로그인 시도: {}", loginRequestDto.getEmail());
//
//        Optional<Member> optionalMember = userRepository.findByEmail(loginRequestDto.getEmail());
//
//        if (optionalMember.isPresent()) {
//            Member member = optionalMember.get();
//            //test
//            if (member == null) {
//                log.warn("이메일 없음: {}", loginRequestDto.getEmail());
//                return null;
//            }
//
//
//            if (bCryptPasswordEncoder.matches(loginRequestDto.getPassword(), member.getPassword())) {
//                return member;
//            } else{
//                log.warn("비밀번호 불일치");
//                return null;
//            }
//        }
//        return null;
//    }


}
