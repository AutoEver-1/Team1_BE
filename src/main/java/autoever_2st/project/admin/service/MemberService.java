package autoever_2st.project.admin.service;


import autoever_2st.project.admin.dto.stats.RegisterStatsDto;
import autoever_2st.project.admin.dto.stats.TotalMemberStatsDto;
import autoever_2st.project.admin.dto.stats.WithdrawalStatsDto;
import autoever_2st.project.admin.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    //가입자
    public RegisterStatsDto getRegisterStats(String dateType) {
        int currentYear = LocalDate.now().getYear();
        List<Map<Integer, Integer>> registerCountList = new ArrayList<>();

        if ("day".equalsIgnoreCase(dateType)) {
            LocalDate today = LocalDate.now();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                int day = date.getDayOfMonth();
                int count = memberRepository.countByCreatedDate(date);
                registerCountList.add(Map.of(day, count));
            }
        } else if ("month".equalsIgnoreCase(dateType)) {
            for (int month = 1; month <= 12; month++) {
                int count = memberRepository.countByCreatedMonth(currentYear, month);
                registerCountList.add(Map.of(month, count));
            }
        } else {
            throw new IllegalArgumentException("Invalid dateType: " + dateType);
        }

        return new RegisterStatsDto(currentYear, registerCountList);
    }

    //탈퇴자
    public WithdrawalStatsDto getWithdrawalStats(String dateType) {
        int currentYear = LocalDate.now().getYear();
        List<Map<Integer, Integer>> withdrawalCountList = new ArrayList<>();

        if ("day".equalsIgnoreCase(dateType)) {
            LocalDate today = LocalDate.now();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                int day = date.getDayOfMonth();
                int count = memberRepository.countByWithdrawalDate(date);
                withdrawalCountList.add(Map.of(day, count));
            }
        } else if ("month".equalsIgnoreCase(dateType)) {
            for (int month = 1; month <= 12; month++) {
                int count = memberRepository.countByWithdrawalMonth(currentYear, month);
                withdrawalCountList.add(Map.of(month, count));
            }
        } else {
            throw new IllegalArgumentException("Invalid dateType: " + dateType);
        }

        return new WithdrawalStatsDto(currentYear, withdrawalCountList);
    }

    //누적 회원수
    public TotalMemberStatsDto getTotalMemberStats() {
        Long total = memberRepository.count();
        return new TotalMemberStatsDto(total);
    }


}
