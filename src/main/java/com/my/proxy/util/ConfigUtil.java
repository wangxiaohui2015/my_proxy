package com.my.proxy.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.my.proxy.Main;
import com.my.proxy.entity.BackendServer;

/**
 * LB configuration utility tool.
 * 
 * @author Sunny
 */
public class ConfigUtil {

    private static Logger logger = Logger.getLogger(ConfigUtil.class);
    private static final ConfigUtil instance = new ConfigUtil();
    private static Properties properties = new Properties();

    private static final String KEY_LB_PROTOCOL = "lb.protocol";
    private static final String KEY_LB_PORT = "lb.port";
    private static final String KEY_LB_MODE = "lb.mode";
    private static final String KEY_LB_THREADS = "lb.threads";
    private static final String KEY_BACKEND_SERVER_PREFIX = "server";

    private String configFilePath = "";
    private String lbProtocol = "tcp";
    private int lbPort = 80;
    private String lbMode = "random";
    private int lbThreads = 8;
    private List<BackendServer> backendServers = new ArrayList<BackendServer>();

    public static final ConfigUtil getInstance() {
        return instance;
    }

    public String getLbProtocol() {
        return lbProtocol;
    }

    public int getLbPort() {
        return lbPort;
    }

    public String getLbMode() {
        return lbMode;
    }

    public int getLbThreads() {
        return lbThreads;
    }

    public List<BackendServer> getBackendServers() {
        return backendServers;
    }

    public void init() throws Exception {
        configFilePath = Main.getRootDir() + File.separator + "conf" + File.separator + "myproxy.properties";
        File configFile = new File(configFilePath);
        if (!configFile.exists()) {
            String errorMsg = "configuration file doesn't exist, " + configFilePath;
            logger.error(errorMsg);
            throw new Exception(errorMsg);
        }

        try {
            InputStream in = new FileInputStream(configFilePath);
            properties.load(in);
            initLBProtocol();
            initLBPort();
            initLBMode();
            initLBThreads();
            initServers();
            checkServers();
        } catch (Exception e) {
            logger.error("Failed to init ServiceConfigPropertiesUtil, service exits.", e);
            throw e;
        }
    }

    private ConfigUtil() {
    }

    private void initLBProtocol() {
        String protocol = properties.getProperty(KEY_LB_PROTOCOL);
        if (null == protocol || "".equals(protocol)) {
            return;
        }
        lbProtocol = protocol;
    }

    private void initLBPort() {
        String port = properties.getProperty(KEY_LB_PORT);
        if (null == port || "".equals(port)) {
            return;
        }
        try {
            lbPort = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse lb port, will use default value.", e);
        }
    }

    private void initLBMode() {
        String mode = properties.getProperty(KEY_LB_MODE);
        if (null == mode || "".equals(mode)) {
            return;
        }
        lbMode = mode;
    }

    private void initLBThreads() {
        String threads = properties.getProperty(KEY_LB_THREADS);
        if (null == threads || "".equals(threads)) {
            return;
        }
        try {
            lbThreads = Integer.parseInt(threads);
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse lb threads, will use default value.", e);
        }
    }

    private List<String> getAllServerKeys() {
        List<String> keys = new ArrayList<String>();
        Set<Object> keySet = properties.keySet();
        Iterator<Object> it = keySet.iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (key.startsWith(KEY_BACKEND_SERVER_PREFIX)) {
                keys.add(key);
            }
        }
        return keys;
    }

    private void initServers() {
        List<String> serverKeys = getAllServerKeys();
        for (String serverKey : serverKeys) {
            String serverConfig = properties.getProperty(serverKey);
            if (null == serverConfig || "".equals(serverConfig)) {
                logger.warn("Empty server configuration, skip." + serverKey);
                continue;
            }
            String[] serverConfigs = serverConfig.split(" ");
            if (serverConfigs.length < 2) {
                logger.warn("Invalid server configuration, need at least 'ip port', server: " + serverKey + "="
                        + serverConfig);
                continue;
            }
            String ip = serverConfigs[0];
            String portStr = serverConfigs[1];
            int port = 0;
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                logger.warn("Invalid port:" + portStr, e);
                continue;
            }
            int weight = 1;
            if (serverConfigs.length >= 3) {
                String weightStr = serverConfigs[2];
                try {
                    weight = Integer.parseInt(weightStr);
                } catch (NumberFormatException e) {
                    logger.warn("Invalid weight:" + weightStr + ", use default," + weight, e);
                }
            }
            boolean isCheck = false;
            if (serverConfigs.length >= 4) {
                String isCheckStr = serverConfigs[3];
                if ("true".equals(isCheckStr)) {
                    isCheck = true;
                }
            }
            String uri = "";
            if (serverConfigs.length >= 5) {
                uri = serverConfigs[4];
            }
            BackendServer bs = new BackendServer(serverKey, ip, port, weight, isCheck, uri);
            backendServers.add(bs);
        }
    }

    private void checkServers() throws Exception {
        if (backendServers.isEmpty()) {
            String errorMsg = "No backend server is configured.";
            logger.error(errorMsg);
            throw new Exception(errorMsg);
        }
    }
}
