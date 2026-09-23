package com.erick.estoque.produto;

import com.erick.estoque.categoria.Categoria;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "produtos")
public class Produto {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @Column(nullable = false)
 private String nome;

 @Column(nullable = false, precision = 12, scale = 2)
 private BigDecimal preco;

 @Column(nullable = false)
 private Integer quantidade = 0;

 @ManyToOne(optional = false)
 @JoinColumn(name = "categoria_id", nullable = false)
 private Categoria categoria;

 private String descricao;

 public Produto() {
 }

 public Long getId() {
  return id;
 }

 public String getNome() {
  return nome;
 }

 public BigDecimal getPreco() {
  return preco;
 }

 public Integer getQuantidade() {
  return quantidade;
 }

 public Categoria getCategoria() {
  return categoria;
 }

 public String getDescricao() {
  return descricao;
 }

 public void setNome(String nome) {
  this.nome = nome;
 }

 public void setPreco(BigDecimal preco) {
  this.preco = preco;
 }

 public void setQuantidade(Integer quantidade) {
  this.quantidade = quantidade;
 }

 public void setCategoria(Categoria categoria) {
  this.categoria = categoria;
 }

 public void setDescricao(String descricao) {
  this.descricao = descricao;
 }
}