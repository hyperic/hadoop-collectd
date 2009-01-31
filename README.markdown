## hadoop-collectd - Hadoop integration for collectd

This package implements a Hadoop org.apache.hadoop.metrics.spi.MetricContext
for sending metric data to collectd.

### Configuration

Set the classpath in conf/hadoop-env.sh:

    export HADOOP_CLASSPATH=collectd.jar:hadoop-collectd.jar

Set the context, collection period and collectd servers in conf/hadoop-metrics.properties:

    # Configuration of the "dfs" context for collectd
    dfs.class=org.apache.hadoop.metrics.collectd.CollectdContext
    dfs.period=10
    dfs.servers=239.192.74.66:25826
    
    # Configuration of the "mapred" context for collectd
    mapred.class=org.apache.hadoop.metrics.collectd.CollectdContext
    mapred.period=10
    mapred.servers=239.192.74.66:25826

    # Configuration of the "jvm" context for collectd
    jvm.class=org.apache.hadoop.metrics.collectd.CollectdContext
    jvm.period=10
    jvm.servers=239.192.74.66:25826

### Links

[hadoop](http://hadoop.apache.org/core/) - Hadoop project

[hadoop-collectd](http://github.com/hyperic/hadoop-collectd) - git repo

[collectd](http://collectd.org) - the system statistics collection daemon

[jcollectd](http://support.hyperic.com/display/hypcomm/jcollectd) - Java integration for collectd

[LICENSE](http://www.apache.org/licenses/LICENSE-2.0) - Apache License, Version 2.0

Hadoop Users <core-user@hadoop.apache.org> - Feedback

