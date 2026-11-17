package com.capstone.adaptive_cloud_load_balancer.selection;

import com.capstone.adaptive_cloud_load_balancer.metrics.HostLoadCalculator;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Selects a destination host for a VM -- used both for the Proposed policy's
 * initial VM placement AND for choosing a migration target. Deliberately
 * shared for both purposes: the selection criteria (feasible + lowest
 * resulting load) are the same in both cases, so a single class avoids
 * duplicating the same logic.
 *
 * Rules (per spec):
 *  1. The source host (if any) is excluded.
 *  2. Only hosts that can actually fit the VM (Host.isSuitableForVm, CloudSim
 *     Plus's own PE/RAM/BW/storage feasibility check) are considered.
 *  3. Among feasible hosts, the one with the LOWEST load AFTER hypothetically
 *     placing the VM is chosen -- not the one with the lowest CURRENT load,
 *     which could immediately become the new bottleneck.
 */
public final class DestinationHostSelector {

    public Optional<Host> selectDestination(Vm vm, List<Host> allHosts, Host sourceHostToExclude) {
        return allHosts.stream()
                .filter(host -> host != sourceHostToExclude)
                .filter(host -> host.isSuitableForVm(vm))
                .min(Comparator.comparingDouble(host -> HostLoadCalculator.loadAfterAddition(host, vm)));
    }

    /** Convenience overload for initial placement, where there is no source host to exclude. */
    public Optional<Host> selectDestination(Vm vm, List<Host> allHosts) {
        return selectDestination(vm, allHosts, null);
    }
}
