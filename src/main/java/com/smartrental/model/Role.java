package com.smartrental.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing user roles in the rental management system.
 * Each role has specific permissions and access levels.
 */
@Getter
@RequiredArgsConstructor
public enum Role {
    
    /**
     * Tenant - rents properties from landlords
     * Permissions: View own leases, make payments, submit maintenance requests, 
     * upload documents, chat with AI assistant
     */
    TENANT("ROLE_TENANT", "Tenant"),
    
    /**
     * Owner - owns and manages properties
     * Permissions: Manage properties, view tenants, collect rent, 
     * handle maintenance requests, view analytics
     */
    OWNER("ROLE_OWNER", "Owner"),
    
    /**
     * Admin - system administrator
     * Permissions: Full system access, user management, system configuration
     */
    ADMIN("ROLE_ADMIN", "Administrator");

    private final String authority;
    private final String displayName;

    /**
     * Check if this role has admin privileges.
     * @return true if ADMIN role
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * Check if this role is a tenant.
     * @return true if TENANT role
     */
    public boolean isTenant() {
        return this == TENANT;
    }

    /**
     * Check if this role is an owner.
     * @return true if OWNER role
     */
    public boolean isOwner() {
        return this == OWNER;
    }
}