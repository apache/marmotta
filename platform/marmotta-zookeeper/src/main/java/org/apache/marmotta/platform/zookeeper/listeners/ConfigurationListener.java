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

package org.apache.marmotta.platform.zookeeper.listeners;

import at.salzburgresearch.nodekeeper.NodeKeeper;
import at.salzburgresearch.nodekeeper.NodeListener;
import at.salzburgresearch.nodekeeper.exception.NodeKeeperException;
import at.salzburgresearch.nodekeeper.model.Node;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.apache.marmotta.platform.zookeeper.api.ZookeeperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class ConfigurationListener extends NodeListener<String> {

    private static Logger log = LoggerFactory.getLogger(ConfigurationListener.class);

    private ConfigurationService configurationService;

    public ConfigurationListener(ConfigurationService configurationService, NodeKeeper nodekeeper) {
        this.configurationService = configurationService;
        this.nodekeeper            = nodekeeper;
    }

    @Override
    public void onNodeCreated(Node<String> node) {
        ConfigurationLevel level = getConfigurationLevel(node);

        String key = node.getPath().substring(node.getPath().lastIndexOf("/")+1);
        String value = node.getData();

        try {
            // look if there is a more specific existing configuration; if yes, issue a message and return
            for(ConfigurationLevel existing : new ConfigurationLevel[]{ConfigurationLevel.GLOBAL, ConfigurationLevel.CLUSTER, ConfigurationLevel.INSTANCE}) {
                if(existing.compareTo(level) > 0) {
                    String exvalue = getConfiguration(key,existing);
                    if(exvalue != null) {
                        log.info("ZOOKEEPER: ignoring configuration option {}, because there is a more specific configuration on level {}", key, existing);
                        return;
                    }
                }
            }

            log.info("ZOOKEEPER: setting configuration option {} = {}", key, value);
            if(value.contains("\n")) {
                configurationService.setListConfiguration(key, Arrays.asList(value.split("\n")));
            } else {
                configurationService.setConfiguration(key,value);
            }
        } catch (InterruptedException | NodeKeeperException | IOException e) {
            log.error("ZOOKEEPER: error reading Zookeeper configuration",e);
        }
    }

    @Override
    public void onNodeUpdated(Node<String> node) {
        onNodeCreated(node);
    }

    @Override
    public void onNodeDeleted(Node<String> node) {
        ConfigurationLevel level = getConfigurationLevel(node);
        String key = node.getPath().substring(node.getPath().lastIndexOf("/")+1);

        try {
            // look if there is a more specific existing configuration; if yes, issue a message and return
            for(ConfigurationLevel existing : new ConfigurationLevel[]{ConfigurationLevel.GLOBAL, ConfigurationLevel.CLUSTER, ConfigurationLevel.INSTANCE}) {
                if(existing.compareTo(level) > 0) {
                    String exvalue = getConfiguration(key,existing);
                    if(exvalue != null) {
                        log.info("ZOOKEEPER: ignoring delete option {}, because there is a more specific configuration on level {}", key, existing);
                        return;
                    }
                }
            }

            // look if there is a more generic existing configuration; if yes, use this configuration instead and return
            for(ConfigurationLevel existing : new ConfigurationLevel[]{ConfigurationLevel.INSTANCE, ConfigurationLevel.CLUSTER, ConfigurationLevel.GLOBAL}) {
                if(existing.compareTo(level) < 0) {
                    String exvalue = getConfiguration(key,existing);
                    if(exvalue != null) {
                        log.info("ZOOKEEPER: iupdating option {} to its more generic value {} at level {}", key, exvalue, existing);
                        configurationService.setConfiguration(key,exvalue);
                        return;
                    }
                }
            }


            log.info("ZOOKEEPER: deleting configuration option {}", key);
            configurationService.removeConfiguration(key);
        } catch (InterruptedException | NodeKeeperException | IOException e) {
            log.error("ZOOKEEPER: error reading Zookeeper configuration",e);
        }
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    /**
     * Return the configuration value for the given key at the given level if it exists, or return null otherwise.
     *
     * @param key
     * @param level
     * @return
     */
    private String getConfiguration(String key, ConfigurationLevel level) throws InterruptedException, IOException, NodeKeeperException {
        String uuid = configurationService.getStringConfiguration(ZookeeperService.ZK_INSTANCE, UUID.randomUUID().toString());
        String cluster = configurationService.getStringConfiguration(ZookeeperService.ZK_CLUSTER, "default");

        String path;
        switch (level) {
            case INSTANCE:
                path = String.format("/marmotta/clusters/%s/instances/%s/config/%s", cluster, uuid, key);
                break;
            case CLUSTER:
                path = String.format("/marmotta/clusters/%s/config/%s", cluster, key);
                break;
            default:
                path = String.format("/marmotta/config/%s", key);
                break;
        }

        Node<String> node = nodekeeper.readNode(path, String.class);

        if(node != null) {
            return node.getData();
        } else {
            return null;
        }
    }

    /**
     * Get the configuration level of the node
     *
     * @param node
     * @return
     */
    private ConfigurationLevel getConfigurationLevel(Node<String> node) {
        if(node.getPath().contains("instances")) {
            return ConfigurationLevel.INSTANCE;
        } else if(node.getPath().contains("clusters")) {
            return ConfigurationLevel.CLUSTER;
        } else {
            return ConfigurationLevel.GLOBAL;
        }
    }

    /**
     * Used for representing the different levels at which a configuration event can happen; if there is an
     * INSTANCE level configuration, it will override CLUSTER and GLOBAL and so on.
     */
    private static enum ConfigurationLevel {
        GLOBAL, CLUSTER, INSTANCE
    }
}
