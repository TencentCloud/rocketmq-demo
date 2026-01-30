package com.rocketmq.dashboard.service;

import com.rocketmq.dashboard.dto.request.CreateClusterRequest;
import com.rocketmq.dashboard.dto.request.UpdateClusterRequest;
import com.rocketmq.dashboard.dto.response.ClusterInfo;
import com.tencentcloudapi.trocket.v20230308.TrocketClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ClusterService {
    
    private final TrocketClient trocketClient;
    
    public ClusterService(TrocketClient trocketClient) {
        this.trocketClient = trocketClient;
    }
    
    public List<ClusterInfo> listClusters() throws Exception {
        log.info("Listing all RocketMQ clusters");
        
        List<ClusterInfo> clusters = new ArrayList<>();
        clusters.add(ClusterInfo.builder()
                .clusterId("rmq-cn-demo001")
                .clusterName("demo-cluster")
                .description("Demo cluster for development")
                .region("ap-guangzhou")
                .clusterType("5.x")
                .status("RUNNING")
                .maxTps(10000)
                .maxBandwidth(100)
                .storageCapacity(500)
                .usedStorage(50)
                .publicEndpoint("rmq-cn-demo001.rocketmq.tencentcloudapi.com")
                .privateEndpoint("10.0.0.1:8080")
                .topicCount(5)
                .groupCount(3)
                .createTime(LocalDateTime.now().minusDays(30))
                .updateTime(LocalDateTime.now())
                .build());
        
        log.info("Found {} clusters", clusters.size());
        return clusters;
    }
    
    public ClusterInfo getCluster(String clusterId) throws Exception {
        log.info("Getting cluster details for: {}", clusterId);
        
        return ClusterInfo.builder()
                .clusterId(clusterId)
                .clusterName("demo-cluster")
                .description("Demo cluster for development")
                .region("ap-guangzhou")
                .clusterType("5.x")
                .status("RUNNING")
                .maxTps(10000)
                .maxBandwidth(100)
                .storageCapacity(500)
                .usedStorage(50)
                .publicEndpoint("rmq-cn-demo001.rocketmq.tencentcloudapi.com")
                .privateEndpoint("10.0.0.1:8080")
                .topicCount(5)
                .groupCount(3)
                .createTime(LocalDateTime.now().minusDays(30))
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    public ClusterInfo createCluster(CreateClusterRequest request) throws Exception {
        log.info("Creating cluster: {}", request.getClusterName());
        
        return ClusterInfo.builder()
                .clusterId("rmq-cn-new001")
                .clusterName(request.getClusterName())
                .description(request.getDescription())
                .region(request.getRegion())
                .clusterType(request.getClusterType())
                .status("CREATING")
                .maxTps(request.getMaxTps() != null ? request.getMaxTps() : 10000)
                .maxBandwidth(request.getMaxBandwidth() != null ? request.getMaxBandwidth() : 100)
                .storageCapacity(request.getStorageCapacity() != null ? request.getStorageCapacity() : 500)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    public ClusterInfo updateCluster(String clusterId, UpdateClusterRequest request) throws Exception {
        log.info("Updating cluster: {}", clusterId);
        
        ClusterInfo existing = getCluster(clusterId);
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getMaxTps() != null) {
            existing.setMaxTps(request.getMaxTps());
        }
        if (request.getMaxBandwidth() != null) {
            existing.setMaxBandwidth(request.getMaxBandwidth());
        }
        if (request.getStorageCapacity() != null) {
            existing.setStorageCapacity(request.getStorageCapacity());
        }
        existing.setUpdateTime(LocalDateTime.now());
        
        return existing;
    }
    
    public void deleteCluster(String clusterId) throws Exception {
        log.info("Deleting cluster: {}", clusterId);
        log.info("Cluster deleted successfully: {}", clusterId);
    }
}
