Additional Notes
================

Alia Settings
-------------
Alia settings are hard-coded for load balancing and consistency in Nemo.

These may be parameterized in a future version.

* Load balancing policy: round robin
* Consistency: quorum

Limitations
-----------
* Maps, set, lists and blobs are currently not supported as partition keys.

CPU & Memory Usage
------------------
Nemo's CPU and memory load is directly related to converting Cassandra data to JSON.

The instantaneous memory requirements are a function of the partition row size * row count * concurrent HTTP requests.

A tailored profile should be obtained prior to running Nemo in operations.  This is easily accomplished:

1. Configure Nemo's environment to point to a live Cassandra keyspace.
2. Run Nemo as a jar file (see `Running <running.rst/>`_).
3. Attach VisualVM or JConsole to the running process.
4. Place Nemo under load and observe the CPU and memory high water marks.

Profiling should be performed on hardware that matches the operational platform capabilities if possible.  Profiling on less capable hardware is also acceptable as this ensures a minimum level of expected performance.

