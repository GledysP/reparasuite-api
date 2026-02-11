package com.reparasuite.api.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reparasuite.api.model.Usuario;

public interface UsuarioRepo extends JpaRepository<Usuario, UUID> {
  Optional<Usuario> findByUsuario(String usuario);
}
