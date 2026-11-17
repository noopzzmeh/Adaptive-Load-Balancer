package com.capstone.adaptive_cloud_load_balancer.infrastructure;

import com.capstone.adaptive_cloud_load_balancer.config.SimulationConfig;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.schedulers.vm.VmSchedulerTimeShared;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the fixed list of {@link Host}s used by every experiment
 * (Proposed / Round Robin / Least Loaded / Best Fit), so all algorithms
 * run against identical hardware, per {@link SimulationConfig}.
 */
public final class HostFactory {

    private HostFactory() {}

    public static List<Host> createHosts() {
        final List<Host> hostList = new ArrayList<>(SimulationConfig.HOST_COUNT);
        for (int i = 0; i < SimulationConfig.HOST_COUNT; i++) {
            hostList.add(createHost());
        }
        return hostList;
    }

    private static Host createHost() {
        final List<Pe> peList = new ArrayList<>(SimulationConfig.HOST_PES);
        for (int i = 0; i < SimulationConfig.HOST_PES; i++) {
            peList.add(new PeSimple(SimulationConfig.HOST_PE_MIPS));
        }

        final Host host = new HostSimple(
                SimulationConfig.HOST_RAM_MB,
                SimulationConfig.HOST_BW_MBPS,
                SimulationConfig.HOST_STORAGE_MB,
                peList);

        // Time-shared VM scheduler: PEs can be shared/preempted between VMs so
        // utilization figures reported by CloudSim Plus reflect actual contention
        // instead of a hard space-partition. Migration support requires this
        // policy family (space-shared hosts cannot host a VM "in migration"
        // alongside its normal share).
        host.setVmScheduler(new VmSchedulerTimeShared());

        return host;
    }
}
