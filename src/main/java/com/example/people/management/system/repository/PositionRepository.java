package com.example.people.management.system.repository;

import com.example.people.management.system.model.position.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface PositionRepository extends JpaRepository<Position, Long> {

    @Query("""
              SELECT COUNT(p) > 0
              FROM Position p
              WHERE p.employee.id = :employeeId
                AND p.startDate <= :newEnd
                AND :newStart <= p.endDate
            """)
    boolean existsOverlapping(
            @Param("employeeId") Long employeeId,
            @Param("newStart") LocalDate newStart,
            @Param("newEnd") LocalDate newEnd
    );

}
