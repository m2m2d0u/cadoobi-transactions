package sn.symmetry.cadoobi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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
     * Find permissions by resource
     */
    List<Permission> findByResource(String resource);

    /**
     * Find permissions by resource and action
     */
    List<Permission> findByResourceAndAction(String resource, String action);

    /**
     * Check if permission exists by code
     */
    boolean existsByCode(String code);
}
