package com.capstone.adaptive_cloud_load_balancer.policies;

import java.util.List;
import java.util.Optional;

import org.cloudsimplus.allocationpolicies.VmAllocationPolicyAbstract;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;

/**
 * Baseline #1: Round Robin initial VM placement.
 *
 * Cycles through the host list starting after the last host used, picking
 * the next host that is actually able to fit the VM (Host.isSuitableForVm).
 * This is a STATIC placement policy only -- it never migrates a VM once
 * placed. See the fair-comparison note in the project README regarding
 * comparing this against the Proposed (migration-based) policy.
 */
public final class VmAllocationPolicyRoundRobin extends VmAllocationPolicyAbstract {

    private int nextHostIndex = 0;

    @Override
    protected Optional<Host> defaultFindHostForVm(Vm vm) {
        final List<Host> hosts = getHostList();
        if (hosts.isEmpty()) {
            return Optional.empty();
        }

        for (int attempts = 0; attempts < hosts.size(); attempts++) {
            final int index = (nextHostIndex + attempts) % hosts.size();
            final Host candidate = hosts.get(index);
            if (candidate.isSuitableForVm(vm)) {
                nextHostIndex = (index + 1) % hosts.size();
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }
}
