package com.erick.estoque.security;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class UserEntity {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @Column(nullable = false, unique = true)
 private String email;

 @Column(nullable = false)
 private String senha;

 public UserEntity() {
 }

 public UserEntity(String email, String senha) {
  this.email = email;
  this.senha = senha;
 }

 public Long getId() {
  return id;
 }

 public String getEmail() {
  return email;
 }

 public void setEmail(String email) {
  this.email = email;
 }

 public String getSenha() {
  return senha;
 }

 public void setSenha(String senha) {
  this.senha = senha;
 }
}