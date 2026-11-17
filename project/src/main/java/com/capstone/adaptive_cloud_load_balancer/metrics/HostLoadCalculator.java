package com.capstone.adaptive_cloud_load_balancer.metrics;

import java.util.List;

import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;

import com.capstone.adaptive_cloud_load_balancer.config.SimulationConfig;

/**
 * THE ONE PLACE the Load formula is implemented.
 * Load = CPU_WEIGHT * cpuUtilization + RAM_WEIGHT * ramUtilization
 *
 * Every other class (overload detection, VM selection, destination selection,
 * migration decision, metrics reporting) must call into this class instead of
 * recomputing load itself -- this is what the project spec calls out as
 * "keep the load calculation consistent across experiments" and fixes the
 * historical bug where loadAfter was estimated from raw CPU utilization only.
 *
 * IMPORTANT, HONESTLY DOCUMENTED ASSUMPTION:
 * - cpuUtilization = host.getCpuPercentUtilization(): a genuinely dynamic
 *   value driven by each Cloudlet's UtilizationModelCpu.
 * - ramUtilization = host.getRam().getPercentUtilization(): in CloudSim Plus,
 *   VM RAM is a fixed reservation (set once via Vm.setRam(...)), not an
 *   elastic, cloudlet-driven quantity the way CPU is. So this term reflects
 *   "how much of the host's RAM capacity is reserved by the VMs currently
 *   placed on it", not a time-varying RAM workload intensity. This is stated
 *   explicitly rather than silently implied to be equivalent to CPU dynamism.
 */
public final class HostLoadCalculator {

    private HostLoadCalculator() {}

    /** Current load of a host, in [0,1] range (can slightly exceed 1 under contention). */
    public static double hostLoad(Host host) {
        final double cpu = host.getCpuPercentUtilization();
        final double ram = host.getRam().getPercentUtilization();
        return SimulationConfig.CPU_WEIGHT * cpu + SimulationConfig.RAM_WEIGHT * ram;
    }

    /**
     * Load imbalance across a set of hosts, defined as the population standard
     * deviation of their individual loads. 0 means every host has identical
     * load; larger values mean load is unevenly spread.
     */
    public static double loadImbalance(List<Host> hosts) {
        if (hosts.isEmpty()) {
            return 0.0;
        }
        final double mean = hosts.stream().mapToDouble(HostLoadCalculator::hostLoad).average().orElse(0.0);
        final double variance = hosts.stream()
                .mapToDouble(h -> Math.pow(hostLoad(h) - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

    /**
     * The share of a host's total capacity (CPU-MIPS-wise and RAM-wise) that a
     * given VM currently placed on some host contributes, expressed already
     * combined through the Load formula's weights. Used to estimate the load
     * of a *different* host if this VM were placed there (or removed from its
     * current host), WITHOUT actually moving the VM.
     */
    public static double vmLoadContribution(Vm vm, Host host) {
        final double vmTotalMips = vm.getMips() * vm.getPesNumber();
        final double hostTotalMips = host.getTotalMipsCapacity();
        final double vmMipsUsed = vm.getCpuPercentUtilization() * vmTotalMips;
        final double cpuShare = hostTotalMips <= 0 ? 0.0 : vmMipsUsed / hostTotalMips;

        final double ramShare = host.getRam().getCapacity() <= 0
                ? 0.0
                : (double) vm.getRam().getCapacity() / host.getRam().getCapacity();

        return SimulationConfig.CPU_WEIGHT * cpuShare + SimulationConfig.RAM_WEIGHT * ramShare;
    }

    /** Estimated load of {@code sourceHost} if {@code vm} were migrated away from it. */
    public static double loadAfterRemoval(Host sourceHost, Vm vm) {
        return Math.max(0.0, hostLoad(sourceHost) - vmLoadContribution(vm, sourceHost));
    }

    /** Estimated load of {@code destinationHost} if {@code vm} were migrated onto it. */
    public static double loadAfterAddition(Host destinationHost, Vm vm) {
        return hostLoad(destinationHost) + vmLoadContribution(vm, destinationHost);
    }
}
