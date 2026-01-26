package kz.event.domain.user.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kz.event.domain.user.entity.Friend;
import kz.event.domain.user.enums.FriendStatus;

@Repository
public interface FriendRepository extends JpaRepository<Friend, UUID> {
    
    @Query("""
        select count(f) > 0
        from Friend f
        where (f.userId = :u1 and f.friendId = :u2)
        or (f.userId = :u2 and f.friendId = :u1)
    """)
    boolean existsBetween(@Param("u1") UUID u1, @Param("u2") UUID u2);

    @Query("""
        select f
        from Friend f
        where (f.userId = :u1 and f.friendId = :u2)
        or (f.userId = :u2 and f.friendId = :u1)
    """)
    Friend getFriendsByIds(@Param("u1") UUID u1, @Param("u2") UUID u2);

    @Query("""
        select f
        from Friend f
        where f.userId = :u or f.friendId = :u
    """)
    List<Friend> getAllFriends(@Param("u") UUID u1);

    @Query("""
        select distinct case
            when f.userId = :u1 then f.friendId
            else f.userId
        end
        from Friend f
        where f.status = :status
          and (f.userId = :u1 or f.friendId = :u1)
          and (case
                when f.userId = :u1 then f.friendId
                else f.userId
               end) in (
                select case
                    when f2.userId = :u2 then f2.friendId
                    else f2.userId
                end
                from Friend f2
                where f2.status = :status
                  and (f2.userId = :u2 or f2.friendId = :u2)
          )
    """)
    List<UUID> getMutualFriends(@Param("u1") UUID u1, @Param("u2") UUID u2, @Param("status") FriendStatus status);
}
