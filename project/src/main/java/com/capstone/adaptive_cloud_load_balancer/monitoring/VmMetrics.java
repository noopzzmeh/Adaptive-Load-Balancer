package com.capstone.adaptive_cloud_load_balancer.monitoring;

/** Plain snapshot of one VM's CPU utilization at one point in simulated time. */
public record VmMetrics(
        double time,
        long vmId,
        double cpuUtilization
) {}
