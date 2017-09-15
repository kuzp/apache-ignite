BLAS off-heap support for Apache Ignite ML
------------------------------------------

Prerequisites:

- linux-x86_64
  (currently the only supported architecture)
- cblas
  Same as for netlib being able to use native BLAS implementation:
  https://github.com/fommil/netlib-java/blob/master/native_system/xbuilds/linux-x86_64/pom.xml
  -- in particular, there should be header file:
     /usr/include/cblas.h

How to build:

See https://apacheignite.readme.io/docs/machine-learning#section-getting-started

  cd <ignite-root>; mvn clean install -DskipTests -Dmaven.javadoc.skip=true -P java8

  cd <ignite-root>/modules/ml; mvn clean package -Dmaven.javadoc.skip=true -Dtest=BlasOffHeapTest -U -Pjava8 -Pml -Plgpl

How to benchmark:

  cd <ignite-root>/modules/ml; mvn clean package -Dmaven.javadoc.skip=true -Dtest=BlasOffHeapBenchmark -U -Pjava8 -Pml -Plgpl

Note: -Plgpl is necessary to pick up native parts of netlib
