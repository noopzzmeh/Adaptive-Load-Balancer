package com.capstone.adaptive_cloud_load_balancer.migration;

import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;

/**
 * Estimates migration cost as the time (in simulated seconds) needed to
 * transfer a VM's memory footprint from source to destination host:
 *
 *     MigrationTime (s) = DataTransferred (MB) / AvailableBandwidth (MBps)
 *
 * ASSUMPTIONS, STATED EXPLICITLY (per project requirement to document, not
 * hide, simplifications):
 *  - DataTransferred is approximated as the VM's allocated RAM size
 *    (Vm.getRam().getCapacity(), in MB). This is the standard simplified
 *    live-migration cost model used in the load-balancing/migration
 *    literature (e.g. Beloglazov & Buyya) -- it is NOT a full pre-copy /
 *    dirty-page-rate model.
 *  - Host/VM bandwidth in this project is configured in Mbps (megabits per
 *    second), per SimulationConfig. To get seconds from a MB quantity, Mbps
 *    is converted to MBps by dividing by 8 (8 bits per byte). Skipping this
 *    conversion is exactly the kind of unit inconsistency the project spec
 *    warned about, so it is done explicitly and commented here.
 *  - AvailableBandwidth is the smaller of the source and destination host's
 *    currently AVAILABLE (not total) bandwidth, since both ends of a live
 *    migration are constrained by whichever link is more saturated.
 *  - This estimate is used ONLY for the benefit-vs-cost migration DECISION
 *    (see MigrationDecisionEngine). It does not itself move any data; the
 *    actual migration timing/resource reservation during the real migration
 *    is computed independently by CloudSim Plus's Datacenter when it carries
 *    out the migration returned by the allocation policy.
 */
public final class MigrationCostEstimator {

    private static final double BITS_PER_BYTE = 8.0;
    /** MBps floor used if both hosts report zero available bandwidth, to avoid division by zero. */
    private static final double MIN_BANDWIDTH_MBPS = 1.0;

    public double estimateMigrationTimeSeconds(Vm vm, Host sourceHost, Host destinationHost) {
        final double availableBandwidthMbps = Math.max(
                MIN_BANDWIDTH_MBPS,
                Math.min(sourceHost.getBw().getAvailableResource(), destinationHost.getBw().getAvailableResource())
        );
        final double availableBandwidthMBps = availableBandwidthMbps / BITS_PER_BYTE;

        final double dataToTransferMB = vm.getRam().getCapacity();

        return dataToTransferMB / availableBandwidthMBps;
    }
}
