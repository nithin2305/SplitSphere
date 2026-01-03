package com.splitsphere.repository;

import com.splitsphere.model.Expense;
import com.splitsphere.model.Group;
import com.splitsphere.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByGroup(Group group);
    List<Expense> findByGroupOrderByCreatedAtDesc(Group group);
    
    @Query("SELECT e FROM Expense e WHERE e.group = :group AND (e.payer = :user OR :user MEMBER OF e.participants)")
    List<Expense> findByGroupAndUser(@Param("group") Group group, @Param("user") User user);
}
