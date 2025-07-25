package autoever_2st.project.admin.controller;

import autoever_2st.project.admin.dto.stats.*;
import autoever_2st.project.admin.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member/admin")
public class AdminStaticController {

    private final MemberService memberService;

    //가입자 추세 조회
    @GetMapping("/stats/register/{dateType}")
    public ResponseEntity<RegisterStatsDto> getRegisterStats(@PathVariable String dateType) {
        RegisterStatsDto response = memberService.getRegisterStats(dateType);
        return ResponseEntity.ok(response);
    }

    //탈퇴자 추세 조회
    @GetMapping("/stats/withdrawal/{dateType}")
    public ResponseEntity<WithdrawalStatsDto> getWithdrawalStats(@PathVariable String dateType) {
        WithdrawalStatsDto response = memberService.getWithdrawalStats(dateType);
        return ResponseEntity.ok(response);
    }

    //누적 회원 수 조회
    @GetMapping("/stats/total")
    public ResponseEntity<TotalMemberStatsDto> getTotalMemberStats() {
        TotalMemberStatsDto response = memberService.getTotalMemberStats();
        return ResponseEntity.ok(response);
    }
}
