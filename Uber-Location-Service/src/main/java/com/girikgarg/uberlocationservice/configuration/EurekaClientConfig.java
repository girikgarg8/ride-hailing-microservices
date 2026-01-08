package com.girikgarg.uberlocationservice.configuration;

import com.netflix.appinfo.DataCenterInfo;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.MyDataCenterInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Collections;

/**
 * Custom Eureka configuration to force localhost (127.0.0.1) registration for local development.
 * 
 * Problem: By default, Eureka detects the machine's network IP (e.g., 192.168.0.6)
 * which may not be routable in all network configurations.
 * 
 * Solution: Create a custom InetUtils that ignores all network interfaces
 * and forces 127.0.0.1 as the IP address.
 */
@Configuration
public class EurekaClientConfig {

    @Value("${server.port}")
    private int serverPort;

    @Value("${spring.application.name}")
    private String appName;

    /**
     * Custom InetUtils that always returns 127.0.0.1 for local development.
     * This prevents Spring Cloud from auto-detecting the machine's network IP.
     */
    @Bean
    @Primary
    public InetUtils localhostInetUtils() {
        InetUtilsProperties properties = new InetUtilsProperties();
        // Ignore all network interfaces to prevent auto-detection
        properties.setPreferredNetworks(Collections.singletonList("127.0"));
        properties.setUseOnlySiteLocalInterfaces(false);
        return new InetUtils(properties);
    }

    @Bean
    public EurekaInstanceConfigBean eurekaInstanceConfig(InetUtils inetUtils) {
        EurekaInstanceConfigBean config = new EurekaInstanceConfigBean(inetUtils);
        
        // Force localhost for local development
        config.setPreferIpAddress(true);
        config.setIpAddress("127.0.0.1");
        config.setHostname("localhost");
        config.setNonSecurePort(serverPort);
        config.setInstanceId(appName + ":" + serverPort);
        
        // Set explicit URLs
        config.setHomePageUrl("http://127.0.0.1:" + serverPort + "/");
        config.setStatusPageUrl("http://127.0.0.1:" + serverPort + "/actuator/info");
        config.setHealthCheckUrl("http://127.0.0.1:" + serverPort + "/actuator/health");
        
        // Ensure ports are configured
        config.setNonSecurePortEnabled(true);
        config.setSecurePortEnabled(false);
        
        System.out.println("🔧 Eureka Instance Config:");
        System.out.println("   IP Address: " + config.getIpAddress());
        System.out.println("   Hostname: " + config.getHostname());
        System.out.println("   Home Page URL: " + config.getHomePageUrl());
        System.out.println("   Prefer IP: " + config.isPreferIpAddress());
        
        return config;
    }
}

