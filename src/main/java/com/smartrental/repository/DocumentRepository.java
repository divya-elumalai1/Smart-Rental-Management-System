package com.smartrental.repository;

import com.smartrental.model.Document;
import com.smartrental.model.DocumentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link Document} entity.
 * Provides lookup queries by user, property, and category.
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    /**
     * Find all documents uploaded by a given user.
     */
    List<Document> findByUserId(UUID userId);

    /**
     * Find all documents associated with a given property.
     */
    List<Document> findByPropertyId(UUID propertyId);

    /**
     * Find all documents of a given category.
     */
    List<Document> findByCategory(DocumentCategory category);
}
