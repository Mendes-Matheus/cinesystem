package com.amenicsystem.infrastructure.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing; // Adicionado

@Configuration
@EnableJpaAuditing // Habilita auditoria automática
@EnableJpaRepositories(basePackages = "com.amenicsystem.infrastructure.persistence")
@EntityScan(basePackages = "com.amenicsystem.infrastructure.persistence")
public class JpaConfig {}