package com.capstone.adaptive_cloud_load_balancer.policies;

import com.capstone.adaptive_cloud_load_balancer.metrics.HostLoadCalculator;
import org.cloudsimplus.allocationpolicies.VmAllocationPolicyAbstract;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;

import java.util.Comparator;
import java.util.Optional;

/**
 * Baseline #2: Least Loaded initial VM placement.
 *
 * Places each VM on the feasible host with the lowest CURRENT load (using the
 * same Load formula as the Proposed policy, for a consistent comparison).
 * STATIC placement only -- never migrates a VM once placed.
 */
public final class VmAllocationPolicyLeastLoaded extends VmAllocationPolicyAbstract {

    @Override
    protected Optional<Host> defaultFindHostForVm(Vm vm) {
        return getHostList().stream()
                .filter(host -> host.isSuitableForVm(vm))
                .min(Comparator.comparingDouble(HostLoadCalculator::hostLoad));
    }
}
