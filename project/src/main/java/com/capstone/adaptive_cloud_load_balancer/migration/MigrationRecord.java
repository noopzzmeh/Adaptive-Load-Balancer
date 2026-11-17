package com.capstone.adaptive_cloud_load_balancer.migration;

/**
 * A single logged migration decision that was carried out, for metrics and
 * result reporting. Plain immutable data holder -- deliberately not a
 * CloudSim Plus type, so metrics code doesn't need simulation objects alive
 * after the run finishes.
 */
public record MigrationRecord(
        double simulationTime,
        long vmId,
        long sourceHostId,
        long destinationHostId,
        double loadImbalanceBefore,
        double loadImbalanceAfter,
        double migrationCostSeconds,
        double decisionValue
) {
    @Override
    public String toString() {
        return String.format(
                "t=%.2fs  Vm#%d  Host#%d -> Host#%d  imbalance %.4f -> %.4f  cost=%.2fs  decisionValue=%.4f",
                simulationTime, vmId, sourceHostId, destinationHostId,
                loadImbalanceBefore, loadImbalanceAfter, migrationCostSeconds, decisionValue);
    }
}
