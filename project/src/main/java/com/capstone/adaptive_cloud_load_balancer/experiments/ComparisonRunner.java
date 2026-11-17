package com.capstone.adaptive_cloud_load_balancer.experiments;

import com.capstone.adaptive_cloud_load_balancer.metrics.ComparisonResult;
import com.capstone.adaptive_cloud_load_balancer.policies.AdaptiveMigrationPolicy;
import com.capstone.adaptive_cloud_load_balancer.policies.VmAllocationPolicyBestFit;
import com.capstone.adaptive_cloud_load_balancer.policies.VmAllocationPolicyLeastLoaded;
import com.capstone.adaptive_cloud_load_balancer.policies.VmAllocationPolicyRoundRobin;

/**
 * Runs the four algorithms required by the project (Proposed, Round Robin,
 * Least Loaded, Best Fit) one after another and prints the comparison.
 */
public final class ComparisonRunner {

    public ComparisonResult runAll() {
        final AlgorithmRunner runner = new AlgorithmRunner();
        final ComparisonResult comparison = new ComparisonResult();

        comparison.add(runner.run("Adaptive Load Balancer (Proposed)", new AdaptiveMigrationPolicy()));
        comparison.add(runner.run("Round Robin", new VmAllocationPolicyRoundRobin()));
        comparison.add(runner.run("Least Loaded", new VmAllocationPolicyLeastLoaded()));
        comparison.add(runner.run("Best Fit", new VmAllocationPolicyBestFit()));

        return comparison;
    }
}
