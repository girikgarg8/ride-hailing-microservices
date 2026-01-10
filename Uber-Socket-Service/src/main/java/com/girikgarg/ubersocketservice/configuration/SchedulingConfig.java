package com.girikgarg.ubersocketservice.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring's scheduled task execution capability.
 * Allows methods annotated with @Scheduled to run at fixed intervals or cron expressions.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    
}
