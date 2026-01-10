package com.splitsphere.repository;

import com.splitsphere.model.Group;
import com.splitsphere.model.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByGroupOrderByCreatedAtDesc(Group group);
}
