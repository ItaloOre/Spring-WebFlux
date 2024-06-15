package com.webflux.papuitalo.app.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.webflux.papuitalo.app.documents.Producto;

public interface ProductoDao extends ReactiveMongoRepository<Producto, String>{
	
}
