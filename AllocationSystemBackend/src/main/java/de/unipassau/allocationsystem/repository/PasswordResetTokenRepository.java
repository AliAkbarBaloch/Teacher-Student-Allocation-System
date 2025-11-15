package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.PasswordResetToken;
import de.unipassau.allocationsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for PasswordResetToken entity operations.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Find a reset token by token string.
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Find all tokens for a specific user.
     */
    List<PasswordResetToken> findByUser(User user);

    /**
     * Find valid (unused and not expired) tokens for a user.
     */
    List<PasswordResetToken> findByUserAndUsedFalseAndExpiryDateAfter(User user, LocalDateTime now);

    /**
     * Delete all expired tokens.
     */
    void deleteByExpiryDateBefore(LocalDateTime now);

    /**
     * Delete all tokens for a specific user.
     */
    void deleteByUser(User user);
}
