package com.flipkart.sherlock.semantic.autosuggest.dao;

import com.flipkart.kloud.config.Bucket;
import com.flipkart.kloud.config.ConfigClient;
import com.flipkart.kloud.config.error.ConfigServiceException;
import com.flipkart.sherlock.semantic.autosuggest.models.Config;
import com.flipkart.sherlock.semantic.autosuggest.utils.IOUtils;
import com.flipkart.sherlock.semantic.autosuggest.utils.JsonSeDe;
import com.flipkart.sherlock.semantic.common.dao.mysql.CompleteTableDao;
import com.flipkart.sherlock.semantic.common.dao.mysql.entity.MysqlConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Created by dhruv.pancholi on 31/05/17.
 */
@Slf4j
@Singleton
public class ConfigsDao extends AbstractReloadableCache<Map<String, Config>> {

    @Inject
    public ConfigsDao(CompleteTableDao completeTableDao, JsonSeDe jsonSeDe) {
        super(completeTableDao, jsonSeDe, 1, TimeUnit.DAYS);
    }

    @Override
    protected Map<String, Config> getFromSource() {
        ConfigClient configClient = new ConfigClient();
        Bucket bucket = null;
        Map<String, Config> configMap = new HashMap<>();
        try {
            String clusterName = getClusterName();
            log.info("Getting configuration for the cluster name: {}", clusterName);
            bucket = configClient.getBucket(clusterName, -1);
            log.info("Config obtained from config service: {}", jsonSeDe.writeValueAsString(bucket));
            Map<String, Object> keys = bucket.getKeys();
            for (String key : keys.keySet()) {
                String configString = bucket.getString(key);
                if (configString == null || configString.isEmpty()) continue;
                Config config = jsonSeDe.readValue(configString, Config.class);
                if (config == null) continue;
                configMap.put(key, config);
            }
        } catch (ConfigServiceException | IOException e) {
            configMap.put("default", new Config());
            log.error("Unable to get config from config service", e);
        }
        if (!configMap.containsKey("default")) configMap.put("default", new Config());
        return configMap;
    }

    private static String getClusterName() {
        return IOUtils.open("/etc/default/soa-cluster-name").readlines().get(0);
    }

    public static MysqlConfig getMysqlConfig() throws IOException, ConfigServiceException {
        String clusterName = getClusterName() + "-db";
        ConfigClient configClient = new ConfigClient();
        Bucket bucket = configClient.getBucket(clusterName, -1);
        String dbHost = bucket.getString("dbHost");
        String dbUser = bucket.getString("dbUser");
        String dbPasswd = bucket.getString("dbPasswd");
        String dbName = bucket.getString("dbName");
        Integer dbPort = bucket.getInt("dbPort");
        if (dbHost == null || dbPort == null || dbUser == null || dbPasswd == null || dbName == null)
            return new MysqlConfig("localhost", 3306, "root", "", "sherlock");
        return new MysqlConfig(dbHost, dbPort, dbUser, dbPasswd, dbName);
    }

    public static void main(String[] args) throws IOException, ConfigServiceException {
        String clusterName = getClusterName();
        System.out.println(clusterName);
    }
}
