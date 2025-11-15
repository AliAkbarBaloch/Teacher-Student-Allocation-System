package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    
    Page<User> findByRole(User.UserRole role, Pageable pageable);
    Page<User> findByAccountStatus(User.AccountStatus status, Pageable pageable);
    Page<User> findByEnabled(boolean enabled, Pageable pageable);
    
    long countByEnabled(boolean enabled);
    long countByAccountStatus(User.AccountStatus status);
    long countByAccountLocked(boolean locked);
    long countByRole(User.UserRole role);
    
    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchUsers(String search, Pageable pageable);
}
