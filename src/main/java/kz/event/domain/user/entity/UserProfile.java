package kz.event.domain.user.entity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import kz.event.domain.user.enums.UserSex;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
public class UserProfile {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "avatar_key")
    private String avatarKey;

    @Column
    private String bio;

    @Column
    private String job;

    @Column
    private String city;

    @Column
    private LocalDate birthdate;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false, columnDefinition = "user_sex")
    private UserSex sex;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private OffsetDateTime updatedAt;

    public UserProfile(UUID userId, String username, String fullName, String bio, String job, String city, LocalDate birthdate, UserSex sex) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.bio = bio;
        this.job = job;
        this.city = city;
        this.birthdate = birthdate;
        this.sex = sex;
    }
}
