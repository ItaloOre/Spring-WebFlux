package com.webflux.papuitalo.app.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.webflux.papuitalo.app.documents.Categoria;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String>{

}