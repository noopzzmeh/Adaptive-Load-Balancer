package com.capstone.adaptive_cloud_load_balancer.infrastructure;

import com.capstone.adaptive_cloud_load_balancer.config.SimulationConfig;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the fixed list of {@link Vm}s (identical for every algorithm run),
 * per {@link SimulationConfig}.
 */
public final class VmFactory {

    private VmFactory() {}

    public static List<Vm> createVms() {
        final List<Vm> vmList = new ArrayList<>(SimulationConfig.VM_COUNT);
        for (int i = 0; i < SimulationConfig.VM_COUNT; i++) {
            vmList.add(createVm());
        }
        return vmList;
    }

    private static Vm createVm() {
        final Vm vm = new VmSimple(SimulationConfig.VM_MIPS, SimulationConfig.VM_PES);
        vm.setRam(SimulationConfig.VM_RAM_MB)
          .setBw(SimulationConfig.VM_BW_MBPS)
          .setSize(SimulationConfig.VM_SIZE_MB);
        // Time-shared cloudlet scheduler lets multiple cloudlets share a VM's PEs,
        // which is what allows CPU utilization to vary meaningfully over time.
        vm.setCloudletScheduler(new CloudletSchedulerTimeShared());
        return vm;
    }
}
