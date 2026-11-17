package com.capstone.adaptive_cloud_load_balancer;

import com.capstone.adaptive_cloud_load_balancer.experiments.ComparisonRunner;
import com.capstone.adaptive_cloud_load_balancer.metrics.ComparisonResult;

public final class Main {

    public static void main(String[] args) {
        final ComparisonRunner comparisonRunner = new ComparisonRunner();
        final ComparisonResult result = comparisonRunner.runAll();
        result.print();
    }
}
