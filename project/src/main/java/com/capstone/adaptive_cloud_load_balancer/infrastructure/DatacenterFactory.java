package com.capstone.adaptive_cloud_load_balancer.infrastructure;

import java.util.List;

import org.cloudsimplus.allocationpolicies.VmAllocationPolicy;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;

import com.capstone.adaptive_cloud_load_balancer.config.SimulationConfig;

/**
 * Builds the single {@link Datacenter} used by an experiment run.
 * Each experiment (Proposed / RR / LeastLoaded / BestFit) creates its OWN
 * Datacenter + Host list + Vm list from scratch, so runs never share state.
 */
public final class DatacenterFactory {

    private DatacenterFactory() {}

    public static Datacenter createDatacenter(CloudSimPlus simulation,
                                                List<Host> hostList,
                                                VmAllocationPolicy allocationPolicy) {
        final Datacenter datacenter = new DatacenterSimple(simulation, hostList, allocationPolicy);
        datacenter.setSchedulingInterval(SimulationConfig.SCHEDULING_INTERVAL_SECONDS);

        // Only meaningful for migration-capable policies; for baseline (non-migration)
        // policies isVmMigrationSupported() is false and this is a no-op.
        if (allocationPolicy.isVmMigrationSupported()) {
            datacenter.enableMigrations();
        }

        return datacenter;
    }
}
