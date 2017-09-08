BLAS off-heap support for Apache Ignite ML
------------------------------------------

Prerequisites:

- linux-x86_64
  (currently the only supported architecture)
- cblas
  Same as for netlib being able to use native BLAS implementation

How to build:

cd <ignite-root>/modules/ml; mvn clean package -Dmaven.javadoc.skip=true -Dtest=BlasOffHeapTest -U -Pjava8 -Pml -Plgpl

How to benchmark:

cd <ignite-root>/modules/ml; mvn clean package -Dmaven.javadoc.skip=true -Dtest=BlasOffHeapBenchmark -U -Pjava8 -Pml -Plgpl

Note: -Plgpl is necessary to pick up native parts of netlib
