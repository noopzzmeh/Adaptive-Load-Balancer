package com.capstone.adaptive_cloud_load_balancer.workloads;

import java.util.List;

import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.cloudlets.Cloudlet;

public final class WorkloadGenerator {

    /** Simulated seconds after which the second batch of cloudlets starts. */
    public static final double SECOND_BATCH_DELAY_SECONDS = 20.0;

    private WorkloadGenerator() {}

    public static void submitWorkload(DatacenterBroker broker, List<Cloudlet> cloudletList) {
        final int half = cloudletList.size() / 2;
        for (int i = 0; i < cloudletList.size(); i++) {
            final Cloudlet cloudlet = cloudletList.get(i);
            if (i >= half) {
                cloudlet.setSubmissionDelay(SECOND_BATCH_DELAY_SECONDS);
            }
        }
        broker.submitCloudletList(cloudletList);
    }
}