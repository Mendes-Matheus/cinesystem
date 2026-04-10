package com.cinesystem.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.cinesystem.infrastructure.persistence")
@EntityScan(basePackages = "com.cinesystem.infrastructure.persistence")
public class JpaConfig {}
