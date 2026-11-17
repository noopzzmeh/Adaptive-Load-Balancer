package com.capstone.adaptive_cloud_load_balancer.metrics;

import com.capstone.adaptive_cloud_load_balancer.migration.MigrationRecord;
import com.capstone.adaptive_cloud_load_balancer.monitoring.ResourceMonitor;
import org.cloudsimplus.cloudlets.Cloudlet;

import java.util.List;

/**
 * Turns raw run data (ResourceMonitor samples + migration log + finished
 * cloudlet list) into a single ExperimentResult.
 */
public final class MetricsCollector {

    private MetricsCollector() {}

    public static ExperimentResult buildResult(String algorithmName,
                                                 ResourceMonitor monitor,
                                                 List<MigrationRecord> migrations,
                                                 List<Cloudlet> finishedCloudlets,
                                                 double simulationFinishTime) {

        final double totalMigrationOverhead = migrations.stream()
                .mapToDouble(MigrationRecord::migrationCostSeconds)
                .sum();

        // Response time = finish time - arrival time, where arrival time is the
        // cloudlet's submission delay -- the only "arrival" concept this
        // project's WorkloadGenerator establishes (see WorkloadGenerator).
        final double averageResponseTime = finishedCloudlets.stream()
                .mapToDouble(c -> c.getFinishTime() - c.getSubmissionDelay())
                .average().orElse(0.0);

        final double throughput = simulationFinishTime > 0
                ? finishedCloudlets.size() / simulationFinishTime
                : 0.0;

        return new ExperimentResult(
                algorithmName,
                monitor.averageCpuUtilization(),
                monitor.averageRamUtilization(),
                monitor.averageLoadImbalance(),
                migrations.size(),
                totalMigrationOverhead,
                averageResponseTime,
                throughput,
                finishedCloudlets.size()
        );
    }
}
