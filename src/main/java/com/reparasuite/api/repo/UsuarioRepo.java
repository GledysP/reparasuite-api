package com.reparasuite.api.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reparasuite.api.model.Usuario;

public interface UsuarioRepo extends JpaRepository<Usuario, UUID> {

  Optional<Usuario> findByUsuario(String usuario);

  Optional<Usuario> findByUsuarioIgnoreCase(String usuario);

  Optional<Usuario> findByEmailIgnoreCase(String email);

  boolean existsByUsuarioIgnoreCase(String usuario);

  boolean existsByEmailIgnoreCase(String email);
}