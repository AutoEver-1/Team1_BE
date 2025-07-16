package autoever_2st.project.admin.controller;

import autoever_2st.project.admin.dto.stats.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/member/admin")
public class AdminStaticController {

    @GetMapping("/stats/register/{dateType}")
    public ResponseEntity<RegisterStatsDto> getRegisterStats(@PathVariable String dateType) {
        int currentYear = LocalDate.now().getYear();

        List<Map<Integer, Integer>> registerCountList = new ArrayList<>();

        if ("day".equals(dateType)) {
            LocalDate today = LocalDate.now();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                int dayOfMonth = date.getDayOfMonth();
                Map<Integer, Integer> dayData = new HashMap<>();
                dayData.put(dayOfMonth, (int) (Math.random() * 41) + 10);
                registerCountList.add(dayData);
            }
        } else {
            for (int month = 1; month <= 12; month++) {
                Map<Integer, Integer> monthData = new HashMap<>();
                monthData.put(month, (int) (Math.random() * 151) + 50);
                registerCountList.add(monthData);
            }
        }

        RegisterStatsDto response = new RegisterStatsDto(currentYear, registerCountList);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/withdrawal/{dateType}")
    public ResponseEntity<WithdrawalStatsDto> getWithdrawalStats(@PathVariable String dateType) {
        int currentYear = LocalDate.now().getYear();

        List<Map<Integer, Integer>> withdrawalCountList = new ArrayList<>();

        if ("day".equals(dateType)) {
            LocalDate today = LocalDate.now();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                int dayOfMonth = date.getDayOfMonth();
                Map<Integer, Integer> dayData = new HashMap<>();
                dayData.put(dayOfMonth, (int) (Math.random() * 15) + 1);
                withdrawalCountList.add(dayData);
            }
        } else {
            for (int month = 1; month <= 12; month++) {
                Map<Integer, Integer> monthData = new HashMap<>();
                monthData.put(month, (int) (Math.random() * 41) + 10);
                withdrawalCountList.add(monthData);
            }
        }

        WithdrawalStatsDto response = new WithdrawalStatsDto(currentYear, withdrawalCountList);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/stats/total")
    public ResponseEntity<TotalMemberStatsDto> getTotalMemberStats() {
        long totalMember = (long) (Math.random() * 10001) + 10000;

        TotalMemberStatsDto response = new TotalMemberStatsDto(totalMember);
        return ResponseEntity.ok(response);
    }
}
