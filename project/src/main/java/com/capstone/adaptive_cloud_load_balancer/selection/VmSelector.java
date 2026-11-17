package com.capstone.adaptive_cloud_load_balancer.selection;

import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;

import java.util.Comparator;
import java.util.Optional;

/**
 * Selects a VM to migrate away from an overloaded host.
 *
 * Kept deliberately simple and interpretable, per the project spec: pick the
 * VM currently consuming the MOST CPU on that host. Removing the biggest CPU
 * consumer gives the largest single-migration reduction in the source host's
 * load, which is the most direct way to relieve an overloaded host.
 */
public final class VmSelector {

    public Optional<Vm> selectVmToMigrate(Host overloadedHost) {
        return overloadedHost.getVmList().stream()
                .filter(vm -> !vm.isInMigration())
                .max(Comparator.comparingDouble(Vm::getCpuPercentUtilization));
    }
}
