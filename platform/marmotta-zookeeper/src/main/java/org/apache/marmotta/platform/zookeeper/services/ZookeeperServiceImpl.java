/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.platform.zookeeper.services;

import at.salzburgresearch.nodekeeper.NodeKeeper;
import at.salzburgresearch.nodekeeper.exception.NodeKeeperException;
import org.apache.commons.lang.StringUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.core.events.ConfigurationServiceInitEvent;
import org.apache.marmotta.platform.zookeeper.api.ZookeeperService;
import org.apache.marmotta.platform.zookeeper.event.ZookeeperInitEvent;
import org.apache.marmotta.platform.zookeeper.listeners.ConfigurationListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.slf4j.Logger;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
@ApplicationScoped
public class ZookeeperServiceImpl implements ZookeeperService {

    @Inject
    private Logger log;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    @Any
    private Event<ZookeeperInitEvent> zookeeperInitEvent;

    private NodeKeeper nodeKeeper;

    private Properties properties;

    private String datacenterIdPath;

    public void initialise(@Observes ConfigurationServiceInitEvent event) throws IOException, InterruptedException {
        log.warn("Activating Marmotta Zookeeper Bridge");

        //read configuration
        final String connectionString = configurationService.getContextParam(ZK_SERVER);

        if(connectionString != null) {

            log.info(" - connection string: {}",connectionString);

            this.properties = new Properties();
            File nkProperties = new File(configurationService.getHome() + File.separator + "nodekeeper.properties");
            if(nkProperties.exists()) {
                properties.load(new FileInputStream(nkProperties));
            }

            // get instance id (if not configured), first try from context parameters, if not given create random
            if(StringUtils.isBlank(configurationService.getStringConfiguration(ZK_INSTANCE))) {
                String instanceId;
                if(StringUtils.isBlank(configurationService.getContextParam(ZK_INSTANCE))) {
                    instanceId = UUID.randomUUID().toString();
                } else {
                    instanceId = configurationService.getContextParam(ZK_INSTANCE);
                }

                configurationService.setConfiguration(ZK_INSTANCE, instanceId);
            }

            // get cluster name (if not configured), first try from context parameters, if not given use "default"
            if(StringUtils.isBlank(configurationService.getStringConfiguration(ZK_CLUSTER))) {
                String clusterId;
                if(StringUtils.isBlank(configurationService.getContextParam(ZK_CLUSTER))) {
                    clusterId = "default";
                } else {
                    clusterId = configurationService.getContextParam(ZK_CLUSTER);
                }

                configurationService.setConfiguration(ZK_CLUSTER, clusterId);
            }


            log.info(" - initialize nodekeeper connection for instance {} (cluster {})",configurationService.getStringConfiguration(ZK_INSTANCE), configurationService.getStringConfiguration(ZK_CLUSTER));



            try {
                nodeKeeper = new NodeKeeper(connectionString,configurationService.getIntConfiguration(ZK_TIMEOUT, 60000),properties, null);

                initZooKeeper();

                String uuid = configurationService.getStringConfiguration(ZK_INSTANCE, UUID.randomUUID().toString());
                String cluster = configurationService.getStringConfiguration(ZK_CLUSTER, "default");

                ConfigurationListener listener = new ConfigurationListener(configurationService, nodeKeeper);

                nodeKeeper.addListener("/marmotta/config/[^/]+", listener );
                nodeKeeper.addListener(String.format("/marmotta/clusters/%s/config/[^/]+", cluster), listener );
                nodeKeeper.addListener(String.format("/marmotta/clusters/%s/instances/%s/config/[^/]+", cluster, uuid), listener );

                nodeKeeper.startListeners();

                log.info("... running");

                zookeeperInitEvent.fire(new ZookeeperInitEvent());
            } catch (KeeperException ex) {
                log.error("could not initialise Zookeeper: {}", ex.getMessage());
            } catch (NodeKeeperException e) {
                log.error("could not initialise NodeKeeper: {}", e.getMessage());
            }
        } else {
            log.warn("no Zookeeper servers configured, Zookeeper Integration not available");
        }

    }

    @PreDestroy
    private void shutdown()  {
        try {
            log.info("ZOOKEEPER: deactivating Zookeeper Bridge ...");
            if(nodeKeeper != null) {
                String cluster = configurationService.getStringConfiguration(ZK_CLUSTER, "default");

                log.info("- removing lock on datacenter id");
                nodeKeeper.getZooKeeper().delete(String.format(datacenterIdPath, cluster),-1);

                log.info(" - closing nodekeeper connection");
                nodeKeeper.shutdown();
                log.info("   ... closed");
            }
            if(properties != null) {
                File nkProperties = new File(configurationService.getHome() + File.separator + "nodekeeper.properties");
                properties.store(new FileOutputStream(nkProperties), "automatic nodekeeper state");
            }
        } catch (InterruptedException | KeeperException | IOException e) {
            log.error("ZOOKEEPER: exception while shutting down Zookeeper connection ({})", e.getMessage());
        }
    }

    /**
     * init zookeeper if necessary
     * @throws org.apache.zookeeper.KeeperException
     * @throws InterruptedException
     */
    private void initZooKeeper() throws KeeperException, InterruptedException, IOException {

        if (nodeKeeper == null || nodeKeeper.getZooKeeper() == null) {
            log.error("ZooKeeper not available");
        } else {
            String uuid = configurationService.getStringConfiguration(ZK_INSTANCE, UUID.randomUUID().toString());
            String cluster = configurationService.getStringConfiguration(ZK_CLUSTER, "default");

            //create base paths
            createNodeIfNotExists("/marmotta");
            createNodeIfNotExists("/marmotta/config");

            createNodeIfNotExists("/marmotta/clusters");
            createNodeIfNotExists(String.format("/marmotta/clusters/%s", cluster));
            createNodeIfNotExists(String.format("/marmotta/clusters/%s/config", cluster));
            createNodeIfNotExists(String.format("/marmotta/clusters/%s/snowflake", cluster));

            createNodeIfNotExists(String.format("/marmotta/clusters/%s/instances", cluster));
            createNodeIfNotExists(String.format("/marmotta/clusters/%s/instances/%s", cluster, uuid));
            createNodeIfNotExists(String.format("/marmotta/clusters/%s/instances/%s/config", cluster, uuid));

            // TODO: check that the datacenter id is not yet occupied

            // configure datacenter id using a sequential ephemeral node
            datacenterIdPath = nodeKeeper.getZooKeeper().create(String.format("/marmotta/clusters/%s/snowflake/id-", cluster),new String("creator:"+uuid).getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            int datacenterId = Integer.parseInt(datacenterIdPath.substring(datacenterIdPath.lastIndexOf("id-")+3)) % 4096;

            log.info("ZOOKEEPER: generated datacenter ID {}", datacenterId);
            configurationService.setIntConfiguration("database.datacenter.id", datacenterId);
        }
    }




    private void createNodeIfNotExists(String path) throws KeeperException, InterruptedException {
        if (nodeKeeper.getZooKeeper().exists(path,false) == null) {
            String uuid = configurationService.getStringConfiguration(ZK_INSTANCE, UUID.randomUUID().toString());

            nodeKeeper.getZooKeeper().create(path,new String("creator:"+uuid).getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }

    }

}
