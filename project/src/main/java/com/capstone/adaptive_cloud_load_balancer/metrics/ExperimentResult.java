package com.capstone.adaptive_cloud_load_balancer.metrics;

/**
 * Aggregated metrics for one algorithm's simulation run.
 *
 * Only metrics the simulation model actually produces are included here.
 * SLA violations are deliberately NOT part of this record: this project
 * defines no SLA/QoS contract (no slametrics JSON, no deadline per
 * cloudlet), so an "SLA violation count" would have to be invented rather
 * than measured -- the project spec explicitly forbids fabricating metrics.
 */
public record ExperimentResult(
        String algorithmName,
        double averageCpuUtilization,
        double averageRamUtilization,
        double averageLoadImbalance,
        int migrationCount,
        double totalMigrationOverheadSeconds,
        double averageResponseTimeSeconds,
        double throughputCloudletsPerSecond,
        int finishedCloudletCount
) {}
