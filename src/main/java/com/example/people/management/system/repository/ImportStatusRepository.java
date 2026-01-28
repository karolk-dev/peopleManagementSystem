package com.example.people.management.system.repository;

import com.example.people.management.system.model.StatusInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportStatusRepository extends JpaRepository<StatusInfo, Long> {

}
