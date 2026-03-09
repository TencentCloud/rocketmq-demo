package com.rocketmq.dashboard.controller;

import com.rocketmq.dashboard.common.Result;
import com.rocketmq.dashboard.dto.response.RegionInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/config")
@Tag(name = "Configuration", description = "APIs for configuration info")
public class ConfigController {

    @GetMapping("/regions")
    @Operation(summary = "Get available regions", description = "List all available Tencent Cloud regions for RocketMQ")
    public Result<List<RegionInfo>> getRegions() {
        List<RegionInfo> regions = new ArrayList<>();

        regions.add(RegionInfo.builder().regionId("ap-guangzhou").regionName("Guangzhou").available(true).build());
        regions.add(RegionInfo.builder().regionId("ap-shanghai").regionName("Shanghai").available(true).build());
        regions.add(RegionInfo.builder().regionId("ap-beijing").regionName("Beijing").available(true).build());
        regions.add(RegionInfo.builder().regionId("ap-chengdu").regionName("Chengdu").available(true).build());
        regions.add(RegionInfo.builder().regionId("ap-chongqing").regionName("Chongqing").available(true).build());
        regions.add(RegionInfo.builder().regionId("ap-nanjing").regionName("Nanjing").available(true).build());
        regions.add(RegionInfo.builder().regionId("ap-hongkong").regionName("Hong Kong").available(true).build());
        regions.add(RegionInfo.builder().regionId("ap-singapore").regionName("Singapore").available(true).build());
        regions.add(RegionInfo.builder().regionId("ap-tokyo").regionName("Tokyo").available(true).build());
        regions.add(RegionInfo.builder().regionId("na-siliconvalley").regionName("Silicon Valley").available(true).build());
        regions.add(RegionInfo.builder().regionId("na-ashburn").regionName("Virginia").available(true).build());
        regions.add(RegionInfo.builder().regionId("eu-frankfurt").regionName("Frankfurt").available(true).build());

        return Result.success(regions);
    }
}
