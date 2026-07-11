package com.fahim1837.auth_gateway.user;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository <User, UUID>{}

    
