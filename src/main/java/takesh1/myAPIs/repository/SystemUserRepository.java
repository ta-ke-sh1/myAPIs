package takesh1.myAPIs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import takesh1.myAPIs.entity.SystemUser;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemUserRepository extends JpaRepository<SystemUser, UUID> {
    @Query("select c from SystemUser c WHERE c.userId = ?1")
    Optional<SystemUser> findBySystemUserId(UUID id);

    SystemUser findByUsername (String username);

}
