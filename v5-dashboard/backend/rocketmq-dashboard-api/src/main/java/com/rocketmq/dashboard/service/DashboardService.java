package com.rocketmq.dashboard.service;

import com.rocketmq.dashboard.dto.response.DashboardOverview;
import com.rocketmq.dashboard.dto.response.DashboardTrends;
import com.rocketmq.dashboard.dto.response.TopLagGroup;
import com.tencentcloudapi.trocket.v20230308.TrocketClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class DashboardService {
    
    private final TrocketClient trocketClient;
    
    public DashboardService(TrocketClient trocketClient) {
        this.trocketClient = trocketClient;
    }
    
    public DashboardOverview getOverview(String clusterId) throws Exception {
        log.info("Getting dashboard overview for cluster: {}", clusterId);
        
        DashboardOverview overview = DashboardOverview.builder()
                .totalClusters(3)
                .totalTopics(25)
                .totalGroups(18)
                .todayMessages(1528000L)
                .totalMessages(152800000L)
                .totalTps(1250.5)
                .totalLag(3520L)
                .onlineProducers(12)
                .onlineConsumers(18)
                .healthStatus("HEALTHY")
                .build();
        
        log.info("Dashboard overview retrieved successfully");
        return overview;
    }
    
    public DashboardTrends getTrends(String clusterId, String timeRange) throws Exception {
        log.info("Getting dashboard trends for cluster: {}, timeRange: {}", clusterId, timeRange);
        
        List<String> labels;
        List<Long> publishCounts;
        List<Long> consumeCounts;
        List<Double> tpsValues;
        List<Long> lagValues;
        
        if ("24h".equals(timeRange)) {
            labels = Arrays.asList("00:00", "02:00", "04:00", "06:00", "08:00", "10:00", 
                    "12:00", "14:00", "16:00", "18:00", "20:00", "22:00");
            publishCounts = Arrays.asList(45000L, 38000L, 42000L, 58000L, 72000L, 85000L,
                    95000L, 88000L, 82000L, 76000L, 68000L, 52000L);
            consumeCounts = Arrays.asList(43000L, 37500L, 41000L, 57000L, 71000L, 84000L,
                    94000L, 87500L, 81500L, 75500L, 67500L, 51500L);
            tpsValues = Arrays.asList(750.5, 633.3, 700.0, 966.7, 1200.0, 1416.7,
                    1583.3, 1466.7, 1366.7, 1266.7, 1133.3, 866.7);
            lagValues = Arrays.asList(2000L, 1500L, 1800L, 2100L, 2300L, 2500L,
                    2800L, 2600L, 2400L, 2200L, 2000L, 1800L);
        } else if ("7d".equals(timeRange)) {
            labels = Arrays.asList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
            publishCounts = Arrays.asList(1200000L, 1350000L, 1280000L, 1420000L, 1380000L, 980000L, 850000L);
            consumeCounts = Arrays.asList(1180000L, 1330000L, 1270000L, 1410000L, 1370000L, 975000L, 845000L);
            tpsValues = Arrays.asList(1388.9, 1562.5, 1481.5, 1643.5, 1597.2, 1134.3, 984.0);
            lagValues = Arrays.asList(20000L, 22000L, 21000L, 23000L, 22500L, 18000L, 15000L);
        } else {
            labels = Arrays.asList("Week 1", "Week 2", "Week 3", "Week 4");
            publishCounts = Arrays.asList(8500000L, 9200000L, 8800000L, 9000000L);
            consumeCounts = Arrays.asList(8450000L, 9150000L, 8750000L, 8950000L);
            tpsValues = Arrays.asList(1395.8, 1511.9, 1446.8, 1479.2);
            lagValues = Arrays.asList(50000L, 55000L, 52000L, 53000L);
        }
        
        DashboardTrends trends = DashboardTrends.builder()
                .labels(labels)
                .publishCounts(publishCounts)
                .consumeCounts(consumeCounts)
                .tpsValues(tpsValues)
                .lagValues(lagValues)
                .build();
        
        log.info("Dashboard trends retrieved successfully for timeRange: {}", timeRange);
        return trends;
    }
    
    public List<TopLagGroup> getTopLagGroups(String clusterId, Integer limit) throws Exception {
        log.info("Getting top lag consumer groups for cluster: {}, limit: {}", clusterId, limit);
        
        List<TopLagGroup> topLagGroups = new ArrayList<>();
        
        topLagGroups.add(TopLagGroup.builder()
                .groupName("slow-consumer-group-1")
                .topicName("high-volume-topic")
                .totalLag(15200L)
                .consumeTps(85.5)
                .lastConsumeTimestamp(System.currentTimeMillis() - 300000)
                .build());
        
        topLagGroups.add(TopLagGroup.builder()
                .groupName("batch-processor-group")
                .topicName("order-events")
                .totalLag(8500L)
                .consumeTps(120.3)
                .lastConsumeTimestamp(System.currentTimeMillis() - 180000)
                .build());
        
        topLagGroups.add(TopLagGroup.builder()
                .groupName("analytics-consumer")
                .topicName("user-behavior-events")
                .totalLag(6200L)
                .consumeTps(95.8)
                .lastConsumeTimestamp(System.currentTimeMillis() - 120000)
                .build());
        
        topLagGroups.add(TopLagGroup.builder()
                .groupName("notification-service")
                .topicName("notification-queue")
                .totalLag(4800L)
                .consumeTps(150.2)
                .lastConsumeTimestamp(System.currentTimeMillis() - 90000)
                .build());
        
        topLagGroups.add(TopLagGroup.builder()
                .groupName("archive-worker")
                .topicName("archive-topic")
                .totalLag(3200L)
                .consumeTps(60.5)
                .lastConsumeTimestamp(System.currentTimeMillis() - 60000)
                .build());
        
        int actualLimit = limit != null && limit > 0 ? Math.min(limit, topLagGroups.size()) : topLagGroups.size();
        List<TopLagGroup> result = topLagGroups.subList(0, actualLimit);
        
        log.info("Found {} top lag consumer groups", result.size());
        return result;
    }
}
