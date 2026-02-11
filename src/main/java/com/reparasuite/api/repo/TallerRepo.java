package com.reparasuite.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reparasuite.api.model.Taller;

public interface TallerRepo extends JpaRepository<Taller, Long> { }
