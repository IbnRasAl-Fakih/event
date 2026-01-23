package kz.event.domain.user.repository;

import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import kz.event.domain.user.entity.UserProfile;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    Boolean existsByUsername(String username);

    @Modifying
    @Transactional
    @Query("update UserProfile p set p.avatarKey = :key where p.userId = :userId")
    int updateAvatarKey(@Param("userId") UUID userId, @Param("key") String key);
}
