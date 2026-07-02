package com.smartrental.repository;

import com.smartrental.model.MaintenanceComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link MaintenanceComment} entity.
 * Provides lookup queries by maintenance request.
 */
@Repository
public interface MaintenanceCommentRepository extends JpaRepository<MaintenanceComment, UUID> {

    /**
     * Find all comments belonging to a given maintenance request.
     */
    List<MaintenanceComment> findByMaintenanceRequestId(UUID maintenanceRequestId);
}
