package com.capstone.adaptive_cloud_load_balancer.detection;

import com.capstone.adaptive_cloud_load_balancer.config.SimulationConfig;
import org.cloudsimplus.hosts.Host;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Requires a host to be overloaded for SimulationConfig.PERSISTENCE_INTERVALS
 * CONSECUTIVE monitoring checks before reporting it as persistently overloaded.
 * This avoids reacting to a single temporary spike.
 *
 * Each host has its OWN independent counter (keyed by host id) -- a single
 * shared counter would be wrong, since hosts become/stop being overloaded
 * independently of each other. This is exactly the fix called out in the
 * project spec.
 */
public final class PersistentOverloadDetector {

    private final OverloadDetector overloadDetector;
    private final Map<Long, Integer> consecutiveOverloadCount = new HashMap<>();

    public PersistentOverloadDetector(OverloadDetector overloadDetector) {
        this.overloadDetector = overloadDetector;
    }

    /**
     * Call once per host per monitoring interval. Updates that host's counter
     * and returns whether the host has now been overloaded for long enough to
     * warrant a migration decision.
     */
    public boolean checkAndUpdate(Host host, List<Host> allHosts) {
        final long hostId = host.getId();
        final boolean overloadedNow = overloadDetector.isOverloaded(host, allHosts);

        if (overloadedNow) {
            final int count = consecutiveOverloadCount.getOrDefault(hostId, 0) + 1;
            consecutiveOverloadCount.put(hostId, count);
            return count >= SimulationConfig.PERSISTENCE_INTERVALS;
        } else {
            consecutiveOverloadCount.put(hostId, 0);
            return false;
        }
    }

    /** Resets a host's counter, e.g. right after a migration has been carried out for it. */
    public void reset(Host host) {
        consecutiveOverloadCount.put(host.getId(), 0);
    }
}
