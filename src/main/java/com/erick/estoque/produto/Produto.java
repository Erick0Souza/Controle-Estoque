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

 @Column(unique = true, length = 50)
 private String sku;

 @Column(nullable = false)
 private String nome;

 @Column(nullable = false, precision = 12, scale = 2)
 private BigDecimal preco;

 @Column(nullable = false)
 private Integer quantidade = 0;

 @Column(name = "estoque_minimo")
 private Integer estoqueMinimo = 0;

 @ManyToOne(optional = false)
 @JoinColumn(
         name = "categoria_id",
         nullable = false
 )
 private Categoria categoria;

 private String descricao;

 @Column(
         name = "imagem_url",
         length = 500
 )
 private String imagemUrl;

 public Produto() {
 }

 public Long getId() {
  return id;
 }

 public String getSku() {
  return sku;
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

 public Integer getEstoqueMinimo() {
  return estoqueMinimo;
 }

 public Categoria getCategoria() {
  return categoria;
 }

 public String getDescricao() {
  return descricao;
 }

 public String getImagemUrl() {
  return imagemUrl;
 }

 public void setSku(
         String sku
 ) {
  this.sku = sku;
 }

 public void setNome(
         String nome
 ) {
  this.nome = nome;
 }

 public void setPreco(
         BigDecimal preco
 ) {
  this.preco = preco;
 }

 public void setQuantidade(
         Integer quantidade
 ) {
  this.quantidade = quantidade;
 }

 public void setEstoqueMinimo(
         Integer estoqueMinimo
 ) {
  this.estoqueMinimo = estoqueMinimo;
 }

 public void setCategoria(
         Categoria categoria
 ) {
  this.categoria = categoria;
 }

 public void setDescricao(
         String descricao
 ) {
  this.descricao = descricao;
 }

 public void setImagemUrl(
         String imagemUrl
 ) {
  this.imagemUrl = imagemUrl;
 }
}