package com.meta.accesscontrol.repository;

import com.meta.accesscontrol.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Optional<User> findByTextId(String textId);

    @Query("SELECT count(u) FROM User u JOIN u.roles r WHERE r.id = :roleId")
    long countByRoleId(@Param("roleId") Integer roleId);
}