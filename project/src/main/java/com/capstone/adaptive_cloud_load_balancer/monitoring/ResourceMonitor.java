package com.capstone.adaptive_cloud_load_balancer.monitoring;

import com.capstone.adaptive_cloud_load_balancer.metrics.HostLoadCalculator;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;

import java.util.ArrayList;
import java.util.List;

/**
 * Samples host and VM resource utilization each time it is invoked (intended
 * to be driven by CloudSimPlus.addOnClockTickListener(...), see Main / the
 * AlgorithmRunner wiring). Used for ALL four algorithms identically, so the
 * "average CPU/RAM/load imbalance" metrics are computed the same way for
 * every comparison run.
 */
public final class ResourceMonitor {

    private final List<HostMetrics> hostSamples = new ArrayList<>();
    private final List<VmMetrics> vmSamples = new ArrayList<>();
    private final List<Double> loadImbalanceSamples = new ArrayList<>();

    public void sample(List<Host> hosts, double time) {
        for (Host host : hosts) {
            final double cpu = host.getCpuPercentUtilization();
            final double ram = host.getRam().getPercentUtilization();
            final double load = HostLoadCalculator.hostLoad(host);
            hostSamples.add(new HostMetrics(time, host.getId(), cpu, ram, load));

            for (Vm vm : host.getVmList()) {
                vmSamples.add(new VmMetrics(time, vm.getId(), vm.getCpuPercentUtilization()));
            }
        }
        loadImbalanceSamples.add(HostLoadCalculator.loadImbalance(hosts));
    }

    public double averageCpuUtilization() {
        return hostSamples.stream().mapToDouble(HostMetrics::cpuUtilization).average().orElse(0.0);
    }

    public double averageRamUtilization() {
        return hostSamples.stream().mapToDouble(HostMetrics::ramUtilization).average().orElse(0.0);
    }

    public double averageLoadImbalance() {
        return loadImbalanceSamples.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    public List<HostMetrics> getHostSamples() {
        return hostSamples;
    }

    public List<VmMetrics> getVmSamples() {
        return vmSamples;
    }
}
