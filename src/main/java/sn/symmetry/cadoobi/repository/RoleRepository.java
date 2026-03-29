package sn.symmetry.cadoobi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}
