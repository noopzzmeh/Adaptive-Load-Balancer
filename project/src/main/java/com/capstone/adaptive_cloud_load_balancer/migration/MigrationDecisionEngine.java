package com.capstone.adaptive_cloud_load_balancer.migration;

import com.capstone.adaptive_cloud_load_balancer.config.SimulationConfig;
import com.capstone.adaptive_cloud_load_balancer.metrics.HostLoadCalculator;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;

import java.util.List;

/**
 * Implements the central migration decision from the project spec:
 *
 *   LoadBalanceImprovement = LoadImbalance_before - LoadImbalance_after
 *   DecisionValue           = LoadBalanceImprovement - LAMBDA * MigrationCost
 *   migrate only if DecisionValue >= DECISION_THRESHOLD
 *
 * "before" and "after" imbalance are both computed over the WHOLE datacenter
 * (not just source/destination host), because moving load between two hosts
 * can change how balanced the datacenter is as a whole, which is what
 * load-imbalance is meant to capture. The "after" value is estimated
 * WITHOUT actually moving the VM, using HostLoadCalculator's
 * loadAfterRemoval/loadAfterAddition helpers (fixing the historical bug of
 * subtracting raw CPU utilization only).
 */
public final class MigrationDecisionEngine {

    public record Decision(
            double imbalanceBefore,
            double imbalanceAfter,
            double improvement,
            double migrationCostSeconds,
            double decisionValue,
            boolean shouldMigrate
    ) {}

    public Decision evaluate(List<Host> allHosts, Host sourceHost, Host destinationHost,
                              Vm vm, double migrationCostSeconds) {
        final double imbalanceBefore = HostLoadCalculator.loadImbalance(allHosts);
        final double imbalanceAfter = hypotheticalImbalanceAfterMigration(allHosts, sourceHost, destinationHost, vm);

        final double improvement = imbalanceBefore - imbalanceAfter;
        final double decisionValue = improvement - SimulationConfig.LAMBDA * migrationCostSeconds;
        final boolean shouldMigrate = decisionValue >= SimulationConfig.DECISION_THRESHOLD;

        return new Decision(imbalanceBefore, imbalanceAfter, improvement, migrationCostSeconds, decisionValue, shouldMigrate);
    }

    private double hypotheticalImbalanceAfterMigration(List<Host> allHosts, Host sourceHost,
                                                         Host destinationHost, Vm vm) {
        final double mean = allHosts.stream()
                .mapToDouble(h -> loadOf(h, sourceHost, destinationHost, vm))
                .average().orElse(0.0);
        final double variance = allHosts.stream()
                .mapToDouble(h -> Math.pow(loadOf(h, sourceHost, destinationHost, vm) - mean, 2))
                .average().orElse(0.0);
        return Math.sqrt(variance);
    }

    private double loadOf(Host host, Host sourceHost, Host destinationHost, Vm vm) {
        if (host.equals(sourceHost)) {
            return HostLoadCalculator.loadAfterRemoval(sourceHost, vm);
        }
        if (host.equals(destinationHost)) {
            return HostLoadCalculator.loadAfterAddition(destinationHost, vm);
        }
        return HostLoadCalculator.hostLoad(host);
    }
}
