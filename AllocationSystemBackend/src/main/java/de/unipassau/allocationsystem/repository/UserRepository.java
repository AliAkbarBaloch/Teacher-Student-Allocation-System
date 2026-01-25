package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity operations.
 * Provides methods for user management, search, and statistics.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    /**
     * Find user by email address.
     * 
     * @param email the email address
     * @return optional containing the user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if a user with the given email exists.
     * 
     * @param email the email address
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Find users by role with pagination.
     * 
     * @param role the user role
     * @param pageable pagination parameters
     * @return page of users with the specified role
     */
    Page<User> findByRole(User.UserRole role, Pageable pageable);
    
    /**
     * Find users by account status with pagination.
     * 
     * @param status the account status
     * @param pageable pagination parameters
     * @return page of users with the specified status
     */
    Page<User> findByAccountStatus(User.AccountStatus status, Pageable pageable);
    
    /**
     * Find users by enabled status with pagination.
     * 
     * @param enabled the enabled status
     * @param pageable pagination parameters
     * @return page of users with the specified enabled status
     */
    Page<User> findByEnabled(boolean enabled, Pageable pageable);
    
    /**
     * Count users by enabled status.
     * 
     * @param enabled the enabled status
     * @return count of users
     */
    long countByEnabled(boolean enabled);
    
    /**
     * Count users by account status.
     * 
     * @param status the account status
     * @return count of users
     */
    long countByAccountStatus(User.AccountStatus status);
    
    /**
     * Count users by account locked status.
     * 
     * @param locked the locked status
     * @return count of users
     */
    long countByAccountLocked(boolean locked);
    
    /**
     * Count users by role.
     * 
     * @param role the user role
     * @return count of users
     */
    long countByRole(User.UserRole role);
    
    /**
     * Search users by email or full name.
     * 
     * @param search the search term
     * @param pageable pagination parameters
     * @return page of matching users
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchUsers(String search, Pageable pageable);
}
