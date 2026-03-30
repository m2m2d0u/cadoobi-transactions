package sn.symmetry.cadoobi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.symmetry.cadoobi.domain.entity.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * Find role by code
     */
    Optional<Role> findByCode(String code);

    /**
     * Find all active roles
     */
    List<Role> findByIsActiveTrue();

    /**
     * Find all active roles with pagination
     */
    Page<Role> findByIsActiveTrue(Pageable pageable);

    /**
     * Find all non-system roles (can be deleted)
     */
    List<Role> findByIsSystemRoleFalse();

    /**
     * Check if role exists by code
     */
    boolean existsByCode(String code);

    /**
     * Find role with permissions eagerly loaded
     */
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.id = :id")
    Optional<Role> findByIdWithPermissions(UUID id);

    /**
     * Find role by code with permissions eagerly loaded
     */
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.code = :code")
    Optional<Role> findByCodeWithPermissions(String code);

    /**
     * Search across name and code (case-insensitive)
     */
    @Query(value = "SELECT r FROM Role r " +
                   "WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "OR LOWER(r.code) LIKE LOWER(CONCAT('%', :search, '%'))",
           countQuery = "SELECT COUNT(r) FROM Role r " +
                        "WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(r.code) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Role> searchRoles(@Param("search") String search, Pageable pageable);

    /**
     * Search with additional active filter
     */
    @Query(value = "SELECT r FROM Role r " +
                   "WHERE r.isActive = true " +
                   "AND (LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "OR LOWER(r.code) LIKE LOWER(CONCAT('%', :search, '%')))",
           countQuery = "SELECT COUNT(r) FROM Role r " +
                        "WHERE r.isActive = true " +
                        "AND (LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(r.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Role> searchActiveRoles(@Param("search") String search, Pageable pageable);
}
