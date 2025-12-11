package com.asg.settings;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.asg"})
@EnableJpaRepositories(basePackages = {"com.asg.common.lib.repository", "com.asg.settings.repository"})
@EntityScan(basePackages = {"com.asg.common.lib.entity", "com.asg.settings.entity"})
public class SettingsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SettingsApplication.class, args);
	}

}
