package com.smartrental.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing so that {@code @CreatedDate} and {@code @LastModifiedDate}
 * fields on {@link com.smartrental.model.BaseEntity} are populated automatically.
 *
 * <p>Without this configuration, Spring will not inject auditing dates and the
 * non-null timestamp columns will reject inserts at the database level.</p>
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
