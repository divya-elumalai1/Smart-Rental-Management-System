/**
 * Data Transfer Objects (DTOs) for the Smart Rental Management System.
 * 
 * This package contains all request and response DTOs used for API communication.
 * DTOs are separated from entities to:
 * - Decouple API contracts from database schema
 * - Control what data is exposed to clients
 * - Enable validation at the API boundary
 * - Support different representations for different use cases
 * 
 * Naming Convention:
 * - *RequestDTO: Input DTOs for create/update operations
 * - *ResponseDTO: Output DTOs for read operations
 * 
 * @author Smart Rental Team
 * @version 1.0.0
 */
package com.smartrental.model.dto;