package com.capstone.adaptive_cloud_load_balancer.config;

/**
 * Single source of truth for every experimental parameter.
 * Every algorithm (Proposed / Round Robin / Least Loaded / Best Fit) reads
 * these same values, so that comparisons between algorithms are made on an
 * identical infrastructure and workload.
 */
public final class SimulationConfig {

    private SimulationConfig() {}

    // ---------------------------------------------------------------
    // Infrastructure size 
    // ---------------------------------------------------------------
    public static final int HOST_COUNT = 3;
    public static final int VM_COUNT = 6;
    public static final int CLOUDLET_COUNT = 12;

    // ---------------------------------------------------------------
    // Host hardware profile
    // ---------------------------------------------------------------
    public static final int HOST_PES = 6;
    public static final long HOST_PE_MIPS = 10_000;
    public static final long HOST_RAM_MB = 32_768;
    public static final long HOST_BW_MBPS = 100_000;
    public static final long HOST_STORAGE_MB = 1_000_000;

    // ---------------------------------------------------------------
    // VM profile
    // ---------------------------------------------------------------
    public static final int VM_PES = 2;
    public static final long VM_MIPS = 10_000;
    public static final long VM_RAM_MB = 8_192;
    public static final long VM_BW_MBPS = 20_000;
    public static final long VM_SIZE_MB = 20_000;

    

    // ---------------------------------------------------------------
    // Load model:  Load = CPU_WEIGHT * cpuUtilization + RAM_WEIGHT * ramUtilization
    // Treated as an experimental parameter, not a universal constant.
    // ---------------------------------------------------------------
    public static final double CPU_WEIGHT = 0.6;
    public static final double RAM_WEIGHT = 0.4;

    // ---------------------------------------------------------------
    // Overload detection
    // ---------------------------------------------------------------
    
    public static final double OVERLOAD_THRESHOLD = 0.28;
    /** A host is considered underloaded (candidate migration source is not exclusively
     *  overload-driven, but this project only migrates away from overloaded hosts). */
    public static final double UNDERLOAD_THRESHOLD = 0.25;
    /** Number of CONSECUTIVE overloaded sampling intervals required before a migration
     *  is even considered ("persistent" overload, not a one-off spike). */
    public static final int PERSISTENCE_INTERVALS = 2;

    // ---------------------------------------------------------------
    // Migration decision:  DecisionValue = LoadBalanceImprovement - LAMBDA * MigrationCost
    // ---------------------------------------------------------------
    public static final double LAMBDA = 0.001;
    public static final double DECISION_THRESHOLD = 0.005;
    /** Migration proceeds only if DecisionValue >= this threshold. */
    //public static final double DECISION_THRESHOLD = 0.02;

    public static final double COOLDOWN_SECONDS = 40.0;
    public static final double SCHEDULING_INTERVAL_SECONDS = 5.0;

    // ---------------------------------------------------------------
    // Simulation control
    // ---------------------------------------------------------------
    public static final long RANDOM_SEED = 42L;
}
