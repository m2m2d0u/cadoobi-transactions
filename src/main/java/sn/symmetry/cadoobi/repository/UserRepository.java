package sn.symmetry.cadoobi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.symmetry.cadoobi.domain.entity.User;
import sn.symmetry.cadoobi.domain.enums.UserStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by reset token
     */
    Optional<User> findByResetToken(String resetToken);

    /**
     * Find users by status
     */
    List<User> findByStatus(UserStatus status);

    /**
     * Find users by status with pagination
     */
    Page<User> findByStatus(UserStatus status, Pageable pageable);

    /**
     * Find users with expiredpassword reset tokens
     */
    List<User> findByResetTokenExpiresAtBefore(Instant now);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find user with roles eagerly loaded
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(UUID id);

    /**
     * Find user by email with roles eagerly loaded
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(String email);

    /**
     * Find user with roles and permissions eagerly loaded
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.roles r " +
           "LEFT JOIN FETCH r.permissions " +
           "WHERE u.id = :id")
    Optional<User> findByIdWithRolesAndPermissions(UUID id);

    /**
     * Find user by email with roles and permissions eagerly loaded (used for JWT auth)
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.roles r " +
           "LEFT JOIN FETCH r.permissions " +
           "WHERE u.email = :email")
    Optional<User> findByEmailWithRolesAndPermissions(@Param("email") String email);

    /**
     * Full-text search across fullName, email, role name and role code (case-insensitive)
     */
    @Query(value = "SELECT DISTINCT u FROM User u LEFT JOIN u.roles r " +
                   "WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "OR LOWER(r.code) LIKE LOWER(CONCAT('%', :search, '%'))",
           countQuery = "SELECT COUNT(DISTINCT u) FROM User u LEFT JOIN u.roles r " +
                        "WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(r.code) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    /**
     * Full-text search with an additional status filter
     */
    @Query(value = "SELECT DISTINCT u FROM User u LEFT JOIN u.roles r " +
                   "WHERE u.status = :status " +
                   "AND (LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                   "OR LOWER(r.code) LIKE LOWER(CONCAT('%', :search, '%')))",
           countQuery = "SELECT COUNT(DISTINCT u) FROM User u LEFT JOIN u.roles r " +
                        "WHERE u.status = :status " +
                        "AND (LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(r.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchUsersByStatus(@Param("search") String search, @Param("status") UserStatus status, Pageable pageable);
}
