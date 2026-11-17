package com.capstone.adaptive_cloud_load_balancer.experiments;

import com.capstone.adaptive_cloud_load_balancer.infrastructure.DatacenterFactory;
import com.capstone.adaptive_cloud_load_balancer.infrastructure.HostFactory;
import com.capstone.adaptive_cloud_load_balancer.infrastructure.VmFactory;
import com.capstone.adaptive_cloud_load_balancer.metrics.ExperimentResult;
import com.capstone.adaptive_cloud_load_balancer.metrics.MetricsCollector;
import com.capstone.adaptive_cloud_load_balancer.migration.MigrationRecord;
import com.capstone.adaptive_cloud_load_balancer.monitoring.ResourceMonitor;
import com.capstone.adaptive_cloud_load_balancer.policies.AdaptiveMigrationPolicy;
import com.capstone.adaptive_cloud_load_balancer.workloads.CloudletFactory;
import com.capstone.adaptive_cloud_load_balancer.workloads.WorkloadGenerator;
import org.cloudsimplus.allocationpolicies.VmAllocationPolicy;
import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;

import java.util.List;

/**
 * Runs a complete, self-contained simulation for a single VmAllocationPolicy
 * and returns its ExperimentResult.
 *
 * This class intentionally replaces four near-duplicate classes
 * (ProposedExperiment / RoundRobinExperiment / LeastLoadedExperiment /
 * BestFitExperiment) that the original project layout listed: since all four
 * would build the exact same hosts/VMs/cloudlets and differ only in which
 * VmAllocationPolicy is plugged in, having one parameterized runner avoids
 * duplicating that setup code four times while still running each algorithm
 * as a fully separate, isolated simulation (new CloudSimPlus instance, new
 * hosts, new VMs, new cloudlets every time -- nothing is shared between runs).
 */
public final class AlgorithmRunner {

    public ExperimentResult run(String algorithmName, VmAllocationPolicy policy) {
        final CloudSimPlus simulation = new CloudSimPlus();

        final List<Host> hostList = HostFactory.createHosts();
        final Datacenter datacenter = DatacenterFactory.createDatacenter(simulation, hostList, policy);

        final DatacenterBroker broker = new DatacenterBrokerSimple(simulation);

        final List<Vm> vmList = VmFactory.createVms();
        broker.submitVmList(vmList);

        final List<Cloudlet> cloudletList = CloudletFactory.createCloudlets();
        WorkloadGenerator.submitWorkload(broker, cloudletList);

        final ResourceMonitor monitor = new ResourceMonitor();
        simulation.addOnClockTickListener(info -> monitor.sample(hostList, simulation.clock()));

        System.out.println();
        System.out.println(">>> Running algorithm: " + algorithmName);
        simulation.start();

        final List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
        final double finishTime = simulation.clock();

        // DIAGNOSTIC: prints every submitted cloudlet's status/VM/start/finish time,
        // via CloudSim Plus's own table builder. Temporary instrumentation to find out
        // WHY fewer than 12 cloudlets are finishing -- remove once that is understood
        // and fixed, it is not part of the project's permanent metrics output.
        System.out.println("--- Cloudlet execution table for " + algorithmName + " (all submitted cloudlets) ---");
        new CloudletsTableBuilder(broker.getCloudletSubmittedList()).build();
        System.out.println("Simulation ended at clock = " + finishTime
                + " s | submitted=" + broker.getCloudletSubmittedList().size()
                + " | finished=" + finishedCloudlets.size()
                + " | isRunning=" + simulation.isRunning());

        final List<MigrationRecord> migrations = (policy instanceof AdaptiveMigrationPolicy amp)
                ? amp.getMigrationHistory()
                : List.of();

        if (!migrations.isEmpty()) {
            System.out.println("Migrations carried out by " + algorithmName + ":");
            migrations.forEach(m -> System.out.println("  " + m));
        }

        return MetricsCollector.buildResult(algorithmName, monitor, migrations, finishedCloudlets, finishTime);
    }
}
