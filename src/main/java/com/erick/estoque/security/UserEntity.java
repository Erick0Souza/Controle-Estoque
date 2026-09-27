package com.erick.estoque.security;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class UserEntity {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @Column(
         name = "nome_usuario",
         unique = true,
         length = 50
 )
 private String nomeUsuario;

 @Column(
         nullable = false,
         unique = true
 )
 private String email;

 @Column(nullable = false)
 private String senha;

 @Enumerated(EnumType.STRING)
 @Column(
         name = "perfil",
         length = 20
 )
 private PerfilUsuario perfil;

 public UserEntity() {
 }

 public UserEntity(
         String nomeUsuario,
         String email,
         String senha
 ) {
  this.nomeUsuario = nomeUsuario;
  this.email = email;
  this.senha = senha;
  this.perfil = PerfilUsuario.CONSULTA;
 }

 public UserEntity(
         String nomeUsuario,
         String email,
         String senha,
         PerfilUsuario perfil
 ) {
  this.nomeUsuario = nomeUsuario;
  this.email = email;
  this.senha = senha;
  this.perfil = perfil;
 }

 public Long getId() {
  return id;
 }

 public String getNomeUsuario() {

  if (
          nomeUsuario == null ||
                  nomeUsuario.isBlank()
  ) {
   return email;
  }

  return nomeUsuario;
 }

 public void setNomeUsuario(
         String nomeUsuario
 ) {
  this.nomeUsuario = nomeUsuario;
 }

 public String getEmail() {
  return email;
 }

 public void setEmail(
         String email
 ) {
  this.email = email;
 }

 public String getSenha() {
  return senha;
 }

 public void setSenha(
         String senha
 ) {
  this.senha = senha;
 }

 public PerfilUsuario getPerfil() {

  if (perfil == null) {
   return PerfilUsuario.OPERADOR;
  }

  return perfil;
 }

 public void setPerfil(
         PerfilUsuario perfil
 ) {
  this.perfil = perfil;
 }
}