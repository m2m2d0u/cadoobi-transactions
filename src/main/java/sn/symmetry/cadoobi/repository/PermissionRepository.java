package sn.symmetry.cadoobi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.symmetry.cadoobi.domain.entity.Permission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    /**
     * Find permission by code
     */
    Optional<Permission> findByCode(String code);

    /**
     * Find all active permissions
     */
    List<Permission> findByIsActiveTrue();

    /**
     * Find all active permissions with pagination
     */
    Page<Permission> findByIsActiveTrue(Pageable pageable);

    /**
     * Find permissions by resource
     */
    List<Permission> findByResource(String resource);

    /**
     * Find permissions by resource with pagination
     */
    Page<Permission> findByResource(String resource, Pageable pageable);

    /**
     * Find permissions by resource and action
     */
    List<Permission> findByResourceAndAction(String resource, String action);

    /**
     * Check if permission exists by code
     */
    boolean existsByCode(String code);

    /**
     * Search across code, name and resource (case-insensitive)
     */
    @Query(value = "SELECT p FROM Permission p " +
                   "WHERE LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "OR LOWER(p.resource) LIKE LOWER(CONCAT('%', :search, '%'))",
           countQuery = "SELECT COUNT(p) FROM Permission p " +
                        "WHERE LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(p.resource) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Permission> searchPermissions(@Param("search") String search, Pageable pageable);

    /**
     * Search with additional active filter
     */
    @Query(value = "SELECT p FROM Permission p " +
                   "WHERE p.isActive = true " +
                   "AND (LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "OR LOWER(p.resource) LIKE LOWER(CONCAT('%', :search, '%')))",
           countQuery = "SELECT COUNT(p) FROM Permission p " +
                        "WHERE p.isActive = true " +
                        "AND (LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(p.resource) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Permission> searchActivePermissions(@Param("search") String search, Pageable pageable);
}
