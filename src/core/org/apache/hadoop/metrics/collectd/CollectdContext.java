/*
 * Copyright 2009 Hyperic, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.apache.hadoop.metrics.collectd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.metrics.ContextFactory;
import org.apache.hadoop.metrics.MetricsException;
import org.apache.hadoop.metrics.spi.AbstractMetricsContext;
import org.apache.hadoop.metrics.spi.OutputRecord;
import org.apache.hadoop.metrics.spi.Util;
import org.collectd.protocol.Network;
import org.collectd.protocol.UdpSender;
import org.collectd.api.ValueList;

/**
 * Context for sending metrics to collectd.
 */
public class CollectdContext extends AbstractMetricsContext {
  private static final Log LOG =
    LogFactory.getLog(CollectdContext.class);

  static final String PLUGIN = "hadoop";
  private static final String PERIOD_PROPERTY = "period";
  private static final String SERVERS_PROPERTY = "servers";
  private static final String DEFAULT_TYPE = "gauge";

  private String instance;
  private UdpSender sender;
  private Properties types = new Properties();

  private void loadTypes()
  {
    final String file = "hadoop-collectd-types.properties";
    InputStream is =
      getClass().getClassLoader().getResourceAsStream(file);

    if (is != null)
    {
      try {
        types.load(is);
      } catch (IOException e) {
        LOG.error("Loading " + file + ": " + e);
      } finally {
        try {
          is.close();
        } catch (IOException e) {}
      }
    }
    else LOG.warn("Unable to find: " + file);
  }

  public void init(String contextName, ContextFactory factory) 
  {
    super.init(contextName, factory);

    String periodStr = getAttribute(PERIOD_PROPERTY);
    if (periodStr != null) {
      int period = 0;
      try {
        period = Integer.parseInt(periodStr);
      } catch (NumberFormatException nfe) {
      }
      if (period <= 0) {
        throw new MetricsException("Invalid period: " + periodStr);
      }
      setPeriod(period);
      loadTypes();
    }

    sender = new UdpSender();
    List<InetSocketAddress> metricsServers = 
      Util.parse(getAttribute(SERVERS_PROPERTY), Network.DEFAULT_PORT); 

    for (InetSocketAddress addr : metricsServers) {
      sender.addServer(addr);
    }

    instance = defaultInstance();
  }

  private String defaultInstance() {
    //-Dhadoop.log.file=logs/hadoop-user-tasktracker-hostname.out
    String name = System.getProperty("hadoop.log.file");
    if (name == null) {
      return null;
    }
    name = new File(name).getName();
    String[] parts = name.split("-");
    if (parts.length >= 3) {
      return parts[2]; //tasktracker
    }
    return null;
  }

  private String getType(String context, String name) {
    return types.getProperty(context + "." + name, DEFAULT_TYPE);
  }

  protected void emitRecord(String contextName,
                            String recordName,
                            OutputRecord outRec)
    throws IOException
  {
    String context = contextName + "." + recordName;      
    String plugin = PLUGIN + "." + context;

    for (String name : outRec.getMetricNames()) {
      Number value = outRec.getMetric(name);
      String type = getType(context, name);
      if (type.equals("NONE")) {
        continue; //consider disabled
      }
      emitMetric(plugin, name, type, value);
    }
  }

  private void emitMetric(String plugin, String name,
                          String type, Number value)
  {
    ValueList vl = new ValueList();

    vl.setTime(System.currentTimeMillis());
    vl.setInterval(getPeriod());
    vl.setPlugin(plugin);
    vl.setPluginInstance(instance);
    vl.setType(type);
    vl.setTypeInstance(name);
    vl.addValue(value);
    sender.dispatch(vl);
  }
}
