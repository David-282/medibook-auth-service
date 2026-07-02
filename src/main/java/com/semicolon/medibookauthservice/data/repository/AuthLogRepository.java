package com.semicolon.medibookauthservice.data.repository;

import com.semicolon.medibookauthservice.data.models.AuthLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuthLogRepository extends JpaRepository<AuthLog, UUID> {
}
