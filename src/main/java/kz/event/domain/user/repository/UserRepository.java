package kz.event.domain.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kz.event.domain.user.entity.User;
import kz.event.domain.user.enums.UserStatus;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Modifying
    @Query("update User u set u.status = :status where u.id = :id")
    int updateStatus(@Param("id") UUID id, @Param("status") UserStatus status);
}
