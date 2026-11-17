package com.capstone.adaptive_cloud_load_balancer.detection;

import com.capstone.adaptive_cloud_load_balancer.config.SimulationConfig;
import com.capstone.adaptive_cloud_load_balancer.metrics.HostLoadCalculator;
import org.cloudsimplus.hosts.Host;

import java.util.List;

/**
 * Provides the load threshold above which a host is considered overloaded.
 *
 * Two modes are supported:
 *  - STATIC (default): always returns SimulationConfig.OVERLOAD_THRESHOLD.
 *    This is the simplest, most interpretable option and is what is used
 *    unless explicitly changed.
 *  - STATISTICAL: threshold = mean(loads) + k * stddev(loads), clamped to a
 *    sane range. This is a standard, well-known statistical technique (NOT
 *    presented as a novel contribution of this project) that lets the
 *    threshold react to how loaded the datacenter is overall, instead of a
 *    single fixed number.
 *
 * "Adaptive" in this project means exactly this -- a configurable, documented
 * threshold policy -- not a learned or optimization-based mechanism.
 */
public final class AdaptiveThreshold {

    public enum Mode { STATIC, STATISTICAL }

    private static final double MIN_THRESHOLD = 0.5;
    private static final double MAX_THRESHOLD = 0.95;
    private static final double K = 1.0;

    private final Mode mode;

    public AdaptiveThreshold(Mode mode) {
        this.mode = mode;
    }

    public static AdaptiveThreshold staticThreshold() {
        return new AdaptiveThreshold(Mode.STATIC);
    }

    /**
     * @param allHosts the full host list, needed only by STATISTICAL mode to
     *                 compute the current mean/stddev of load across the datacenter.
     */
    public double getThreshold(List<Host> allHosts) {
        if (mode == Mode.STATIC) {
            return SimulationConfig.OVERLOAD_THRESHOLD;
        }

        final double mean = allHosts.stream().mapToDouble(HostLoadCalculator::hostLoad).average().orElse(0.0);
        final double variance = allHosts.stream()
                .mapToDouble(h -> Math.pow(HostLoadCalculator.hostLoad(h) - mean, 2))
                .average().orElse(0.0);
        final double stddev = Math.sqrt(variance);

        final double threshold = mean + K * stddev;
        return Math.min(MAX_THRESHOLD, Math.max(MIN_THRESHOLD, threshold));
    }
}
