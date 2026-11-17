package com.capstone.adaptive_cloud_load_balancer.policies;

import org.cloudsimplus.allocationpolicies.VmAllocationPolicyAbstract;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;

import java.util.Comparator;
import java.util.Optional;

/**
 * Baseline #3: Best Fit initial VM placement.
 *
 * Classic bin-packing Best Fit: among feasible hosts, place the VM on the one
 * that would be left with the SMALLEST leftover free PE capacity -- i.e. the
 * "tightest fit". STATIC placement only -- never migrates a VM once placed.
 */
public final class VmAllocationPolicyBestFit extends VmAllocationPolicyAbstract {

    @Override
    protected Optional<Host> defaultFindHostForVm(Vm vm) {
        return getHostList().stream()
                .filter(host -> host.isSuitableForVm(vm))
                .min(Comparator.comparingLong(host -> host.getFreePesNumber() - vm.getPesNumber()));
    }
}
