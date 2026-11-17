package com.capstone.adaptive_cloud_load_balancer.workloads;

/**
 * Fixed CPU/RAM utilization patterns assigned to cloudlets. Kept intentionally
 * simple (two constant-utilization tiers) so the experiment is easy to reason
 * about and to explain in a viva -- no hidden randomness in what "high" and
 * "low" utilization mean.
 */
public enum WorkloadProfile {

    /**
     * Represents a resource-hungry application (e.g. batch/compute job).
     */
    HIGH(0.95, 0.40),

    /** Represents a light application (e.g. idle web server). */
    LOW(0.20, 0.20);

    private final double cpuUtilization;
    private final double ramUtilization;

    WorkloadProfile(
            double cpuUtilization,
            double ramUtilization) {

        this.cpuUtilization = cpuUtilization;
        this.ramUtilization = ramUtilization;
    }

    public double getCpuUtilization() {
        return cpuUtilization;
    }

    public double getRamUtilization() {
        return ramUtilization;
    }
}