package com.splitsphere.repository;

import com.splitsphere.model.Group;
import com.splitsphere.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByJoinCode(String joinCode);
    List<Group> findByMembersContaining(User user);
    boolean existsByJoinCode(String joinCode);
}
