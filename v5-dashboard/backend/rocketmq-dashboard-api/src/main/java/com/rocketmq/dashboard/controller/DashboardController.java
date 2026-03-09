package com.rocketmq.dashboard.controller;

import com.rocketmq.dashboard.common.Result;
import com.rocketmq.dashboard.dto.response.DashboardOverview;
import com.rocketmq.dashboard.dto.response.DashboardTrends;
import com.rocketmq.dashboard.dto.response.TopLagGroup;
import com.rocketmq.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "APIs for dashboard overview and statistics")
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    
    @GetMapping("/overview")
    @Operation(summary = "Get overview statistics", description = "Retrieve dashboard overview with key metrics")
    public Result<DashboardOverview> getOverview(
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @RequestParam String clusterId) {
        try {
            log.info("Getting dashboard overview for cluster: {}", clusterId);
            DashboardOverview overview = dashboardService.getOverview(clusterId);
            return Result.success(overview);
        } catch (Exception e) {
            log.error("Failed to get dashboard overview", e);
            throw new RuntimeException("Failed to get dashboard overview: " + e.getMessage(), e);
        }
    }
    
    @GetMapping("/trends")
    @Operation(summary = "Get trends data", description = "Retrieve time-series trends data for messages, TPS, and lag")
    public Result<DashboardTrends> getTrends(
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @RequestParam String clusterId,
            @Parameter(description = "Time range: 24h, 7d, 30d", example = "24h")
            @RequestParam(defaultValue = "24h") String timeRange) {
        try {
            log.info("Getting dashboard trends for cluster: {}, timeRange: {}", clusterId, timeRange);
            DashboardTrends trends = dashboardService.getTrends(clusterId, timeRange);
            return Result.success(trends);
        } catch (Exception e) {
            log.error("Failed to get dashboard trends", e);
            throw new RuntimeException("Failed to get dashboard trends: " + e.getMessage(), e);
        }
    }
    
    @GetMapping("/top-lag")
    @Operation(summary = "Get top lag consumer groups", description = "Retrieve consumer groups with highest message lag")
    public Result<List<TopLagGroup>> getTopLagGroups(
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @RequestParam String clusterId,
            @Parameter(description = "Limit number of results", example = "10")
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            log.info("Getting top lag consumer groups for cluster: {}, limit: {}", clusterId, limit);
            List<TopLagGroup> topLagGroups = dashboardService.getTopLagGroups(clusterId, limit);
            return Result.success(topLagGroups);
        } catch (Exception e) {
            log.error("Failed to get top lag consumer groups", e);
            throw new RuntimeException("Failed to get top lag consumer groups: " + e.getMessage(), e);
        }
    }
}
