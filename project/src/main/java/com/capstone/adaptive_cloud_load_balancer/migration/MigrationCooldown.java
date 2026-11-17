package com.capstone.adaptive_cloud_load_balancer.migration;

import com.capstone.adaptive_cloud_load_balancer.config.SimulationConfig;
import org.cloudsimplus.vms.Vm;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple per-VM cooldown: after a VM is migrated, that SAME VM cannot be
 * migrated again until SimulationConfig.COOLDOWN_SECONDS have passed.
 *
 * This is a deliberately minimal implementation of hysteresis: since a
 * migration only ever moves ONE vm at a time, blocking that vm from moving
 * again for a while already prevents the "Host A -> Host B -> Host A" flapping
 * pattern the project spec describes, without needing a separate host-pair
 * tracking structure.
 */
public final class MigrationCooldown {

    private final Map<Long, Double> lastMigrationTime = new HashMap<>();

    public boolean isInCooldown(Vm vm, double currentSimulationTime) {
        final Double last = lastMigrationTime.get(vm.getId());
        if (last == null) {
            return false;
        }
        return (currentSimulationTime - last) < SimulationConfig.COOLDOWN_SECONDS;
    }

    public void recordMigration(Vm vm, double currentSimulationTime) {
        lastMigrationTime.put(vm.getId(), currentSimulationTime);
    }
}
