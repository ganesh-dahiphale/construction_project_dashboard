package com.example.dashboard.repository;

import com.example.dashboard.model.JobStatus;
import com.example.dashboard.model.Role;
import com.example.dashboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    long countByRoleAndJobStatus(Role role, JobStatus jobStatus);

    @Query("SELECT u FROM User u WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            " LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            " LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            " LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:role IS NULL OR u.role = :role) AND " +
            "(:status IS NULL OR u.jobStatus = :status) " +
            "ORDER BY u.id ASC")
    List<User> searchUsers(@Param("search") String search,
                           @Param("role") Role role,
                           @Param("status") JobStatus status);
}
