package com.capstone.adaptive_cloud_load_balancer.workloads;

import java.util.ArrayList;
import java.util.List;

import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.utilizationmodels.UtilizationModelDynamic;

import com.capstone.adaptive_cloud_load_balancer.config.SimulationConfig;

public final class CloudletFactory {

    private static final long CLOUDLET_LENGTH_MI = 60_000;
    private static final int CLOUDLET_PES = 2;
    private static final long CLOUDLET_FILE_SIZE_KB = 300;
    private static final long CLOUDLET_OUTPUT_SIZE_KB = 300;

    private CloudletFactory() {}

    public static List<Cloudlet> createCloudlets() {

        List<Cloudlet> cloudletList =
                new ArrayList<>(SimulationConfig.CLOUDLET_COUNT);

        for (int i = 0;
             i < SimulationConfig.CLOUDLET_COUNT;
             i++) {

            WorkloadProfile profile;

            if (i == 0 || i == 3 || i == 6 || i == 9) {
                profile = WorkloadProfile.HIGH;
            } else {
                profile = WorkloadProfile.LOW;
            }

            Cloudlet cloudlet =
                    createCloudlet(profile);

            cloudletList.add(cloudlet);
        }

        return cloudletList;
    }

    private static Cloudlet createCloudlet(
            WorkloadProfile profile) {

        Cloudlet cloudlet =
                new CloudletSimple(
                        CLOUDLET_LENGTH_MI,
                        CLOUDLET_PES
                );

        cloudlet.setFileSize(CLOUDLET_FILE_SIZE_KB)
                .setOutputSize(CLOUDLET_OUTPUT_SIZE_KB);

        cloudlet.setUtilizationModelCpu(
                new UtilizationModelDynamic(
                        profile.getCpuUtilization()
                )
        );

        cloudlet.setUtilizationModelRam(
                new UtilizationModelDynamic(
                        profile.getRamUtilization()
                )
        );

        cloudlet.setUtilizationModelBw(
                new UtilizationModelDynamic(0.40)
        );

        return cloudlet;
    }
}
