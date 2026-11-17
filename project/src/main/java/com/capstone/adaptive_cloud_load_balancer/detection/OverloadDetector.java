package com.capstone.adaptive_cloud_load_balancer.detection;

import com.capstone.adaptive_cloud_load_balancer.metrics.HostLoadCalculator;
import org.cloudsimplus.hosts.Host;

import java.util.List;

/**
 * Stateless, single-instant overload check: is this host's CURRENT load at or
 * above the threshold? This class has no memory of previous checks -- that is
 * exactly what {@link PersistentOverloadDetector} adds on top of it, so a
 * single temporary spike does not by itself trigger anything.
 */
public final class OverloadDetector {

    private final AdaptiveThreshold threshold;

    public OverloadDetector(AdaptiveThreshold threshold) {
        this.threshold = threshold;
    }

    public boolean isOverloaded(Host host, List<Host> allHosts) {
        return HostLoadCalculator.hostLoad(host) >= threshold.getThreshold(allHosts);
    }
}
