package com.capstone.adaptive_cloud_load_balancer.monitoring;

/** Plain snapshot of one host's utilization at one point in simulated time. */
public record HostMetrics(
        double time,
        long hostId,
        double cpuUtilization,
        double ramUtilization,
        double load
) {}
