package com.capstone.adaptive_cloud_load_balancer.policies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.cloudsimplus.allocationpolicies.migration.VmAllocationPolicyMigrationAbstract;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;

import com.capstone.adaptive_cloud_load_balancer.config.SimulationConfig;
import com.capstone.adaptive_cloud_load_balancer.detection.AdaptiveThreshold;
import com.capstone.adaptive_cloud_load_balancer.detection.OverloadDetector;
import com.capstone.adaptive_cloud_load_balancer.detection.PersistentOverloadDetector;
import com.capstone.adaptive_cloud_load_balancer.migration.MigrationCooldown;
import com.capstone.adaptive_cloud_load_balancer.migration.MigrationCostEstimator;
import com.capstone.adaptive_cloud_load_balancer.migration.MigrationDecisionEngine;
import com.capstone.adaptive_cloud_load_balancer.migration.MigrationRecord;
import com.capstone.adaptive_cloud_load_balancer.selection.DestinationHostSelector;
import com.capstone.adaptive_cloud_load_balancer.selection.VmSelector;

/**
 * The Proposed algorithm. Implements the workflow described in the project
 * spec end to end:
 *
 *   Monitor -> Detect persistent overload -> Select VM -> Select destination
 *   -> Estimate migration cost -> Evaluate benefit vs cost -> Migrate if
 *   beneficial -> Cooldown -> continue monitoring
 *
 */
public final class AdaptiveMigrationPolicy extends VmAllocationPolicyMigrationAbstract {

    private final PersistentOverloadDetector persistentOverloadDetector;
    private final VmSelector vmSelector = new VmSelector();
    private final DestinationHostSelector destinationHostSelector = new DestinationHostSelector();
    private final MigrationCostEstimator migrationCostEstimator = new MigrationCostEstimator();
    private final MigrationDecisionEngine migrationDecisionEngine = new MigrationDecisionEngine();
    private final MigrationCooldown migrationCooldown = new MigrationCooldown();
    private final List<MigrationRecord> migrationHistory = new ArrayList<>();

    public AdaptiveMigrationPolicy() {
        // Required by the parent constructor; unused directly since this class
        // fully overrides getOptimizedAllocationMap() below with its own
        // detect/select/decide pipeline instead of the framework's default
        // power-usage-based algorithm.
        super(host -> Optional.empty());
        this.persistentOverloadDetector = new PersistentOverloadDetector(
                new OverloadDetector(AdaptiveThreshold.staticThreshold()));
    }

    /**
     * Required by the VmAllocationPolicyMigration interface contract, but NOT
     * used by this policy's own overload logic -- overload detection here is
     * entirely driven by PersistentOverloadDetector / HostLoadCalculator
     * (CPU+RAM combined, with persistence), not this single-host,
     * CPU-utilization-only hook. Returning the same static threshold keeps
     * the framework's own isOverloaded()/isUnderOrOverloaded() helper methods
     * (which this class does not call, but which remain part of the public
     * API surface) consistent with SimulationConfig rather than left at the
     * library's unrelated default.
     */
    @Override
    public double getOverUtilizationThreshold(Host host) {
        return SimulationConfig.OVERLOAD_THRESHOLD;
    }

    /** Initial VM placement (also reused as the strategy for choosing a migration target). */
    @Override
    protected Optional<Host> defaultFindHostForVm(Vm vm) {
        return destinationHostSelector.selectDestination(vm, getHostList());
    }

    @Override
    public Map<Vm, Host> getOptimizedAllocationMap(List<? extends Vm> vmList) {
        System.out.println(">>> AdaptiveMigrationPolicy called at time " + getDatacenter().getSimulation().clock());
        final Map<Vm, Host> migrationMap = new HashMap<>();
        final List<Host> allHosts = getHostList();
        final double currentTime = getDatacenter().getSimulation().clock();

        for (Host host : allHosts) {
            final boolean persistentlyOverloaded =
                    persistentOverloadDetector.checkAndUpdate(host, allHosts);

            System.out.println(
                    "HOST CHECK | time=" + currentTime
                    + " | host=" + host.getId()
                    + " | load=" + com.capstone.adaptive_cloud_load_balancer.metrics.HostLoadCalculator.hostLoad(host)
                    + " | overloaded=" + persistentlyOverloaded
            );

            if (!persistentlyOverloaded) {
                continue;
            }

            final Optional<Vm> vmToMigrate = vmSelector.selectVmToMigrate(host);

            System.out.println(
                    "VM SELECTION | time=" + currentTime
                    + " | host=" + host.getId()
                    + " | vmFound=" + vmToMigrate.isPresent()
            );

            if (vmToMigrate.isEmpty()) {
                continue;
            }

            final Vm vm = vmToMigrate.get();

            if (migrationCooldown.isInCooldown(vm, currentTime)) {
                System.out.println(
                        "COOLDOWN BLOCK | time=" + currentTime
                        + " | host=" + host.getId()
                        + " | vm=" + vm.getId()
                );
                continue;
            }

            final Optional<Host> destination =
                    destinationHostSelector.selectDestination(vm, allHosts, host);

            System.out.println(
                    "DESTINATION SELECTION | time=" + currentTime
                    + " | source=" + host.getId()
                    + " | vm=" + vm.getId()
                    + " | destinationFound=" + destination.isPresent()
            );

            if (destination.isEmpty()) {
                continue;
            }

            final Host destHost = destination.get();

            final double migrationCostSeconds =
                    migrationCostEstimator.estimateMigrationTimeSeconds(
                            vm, host, destHost);

            final MigrationDecisionEngine.Decision decision =
                    migrationDecisionEngine.evaluate(
                            allHosts,
                            host,
                            destHost,
                            vm,
                            migrationCostSeconds);

            System.out.println(
                    "MIGRATION CHECK | time=" + currentTime
                    + " | source=" + host.getId()
                    + " | vm=" + vm.getId()
                    + " | destination=" + destHost.getId()
                    + " | cost=" + migrationCostSeconds
                    + " | improvement=" + decision.improvement()
                    + " | decisionValue=" + decision.decisionValue()
                    + " | migrate=" + decision.shouldMigrate()
            );

            if (decision.shouldMigrate()) {
                migrationMap.put(vm, destHost);
                migrationCooldown.recordMigration(vm, currentTime);
                persistentOverloadDetector.reset(host);

                migrationHistory.add(new MigrationRecord(
                        currentTime,
                        vm.getId(),
                        host.getId(),
                        destHost.getId(),
                        decision.imbalanceBefore(),
                        decision.imbalanceAfter(),
                        migrationCostSeconds,
                        decision.decisionValue()));
            }
        }

        return migrationMap;
    }

    public List<MigrationRecord> getMigrationHistory() {
        return migrationHistory;
    }
}
