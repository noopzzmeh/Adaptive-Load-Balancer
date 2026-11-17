package com.capstone.adaptive_cloud_load_balancer.metrics;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects one {@link ExperimentResult} per algorithm and prints them side by
 * side. Deliberately does NOT declare a "winner" -- the project spec requires
 * results to be presented objectively.
 */
public final class ComparisonResult {

    private final List<ExperimentResult> results = new ArrayList<>();

    public void add(ExperimentResult result) {
        results.add(result);
    }

    public List<ExperimentResult> getResults() {
        return results;
    }

    public void print() {
        System.out.println();
        System.out.println("========== EXPERIMENT RESULTS ==========");
        for (ExperimentResult r : results) {
            System.out.println();
            System.out.println("Algorithm: " + r.algorithmName());
            System.out.printf("Average CPU utilization:     %.2f%%%n", r.averageCpuUtilization() * 100);
            System.out.printf("Average RAM utilization:     %.2f%%%n", r.averageRamUtilization() * 100);
            System.out.printf("Average Load Imbalance:      %.4f%n", r.averageLoadImbalance());
            System.out.printf("Migrations performed:        %d%n", r.migrationCount());
            System.out.printf("Total migration overhead:    %.2f s%n", r.totalMigrationOverheadSeconds());
            System.out.printf("Average response time:       %.2f s%n", r.averageResponseTimeSeconds());
            System.out.printf("Throughput:                  %.4f cloudlets/s%n", r.throughputCloudletsPerSecond());
            System.out.printf("Cloudlets finished:          %d / expected 12%n", r.finishedCloudletCount());
        }
        System.out.println();
        System.out.println("=========================================");
        System.out.println("Note: Round Robin / Least Loaded / Best Fit are STATIC initial-placement");
        System.out.println("policies with no migration; the Proposed policy additionally migrates VMs");
        System.out.println("at runtime. See README 'Fair Comparison' section before interpreting deltas");
        System.out.println("as being purely about placement quality.");
        System.out.println("SLA violations are NOT reported: no SLA/QoS model is defined in this project.");
    }
}
