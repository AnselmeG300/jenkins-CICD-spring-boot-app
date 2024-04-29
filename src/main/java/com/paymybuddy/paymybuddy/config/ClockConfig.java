package com.paymybuddy.paymybuddy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {
	/**
	 * Creates a Clock bean to have system's default zone for LocalDate.now().
	 * @return a clock.
	 */
	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone();
	}

}
