package kz.event.domain.user.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import kz.event.domain.user.entity.Friends;

@Repository
public interface FriendRepository extends JpaRepository<Friends, UUID> {
    
    @Query("""
        select count(f) > 0
        from Friends f
        where (f.userId = :u1 and f.friendId = :u2)
        or (f.userId = :u2 and f.friendId = :u1)
    """)
    boolean existsBetween(UUID u1, UUID u2);
}
