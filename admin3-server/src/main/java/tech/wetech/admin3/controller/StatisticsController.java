package tech.wetech.admin3.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.wetech.admin3.sys.service.StatisticsService;
import tech.wetech.admin3.sys.service.dto.DashboardDTO;

/**
 * 数据看板统计 Controller
 */
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

  private final StatisticsService statisticsService;

  public StatisticsController(StatisticsService statisticsService) {
    this.statisticsService = statisticsService;
  }

  @GetMapping("/dashboard")
  public ResponseEntity<DashboardDTO> getDashboard() {
    return ResponseEntity.ok(statisticsService.getDashboard());
  }
}
