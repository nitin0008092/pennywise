package com.pennywise.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic CRUD repository contract demonstrating OOP Abstraction and Generics.
 *
 * @param <T>  Entity type
 * @param <ID> Primary key identifier type
 */
public interface CrudRepository<T, ID> {

    /**
     * Persists a new entity to the database and populates its generated primary key.
     */
    T save(T entity);

    /**
     * Finds an entity by its primary key ID.
     */
    Optional<T> findById(ID id);

    /**
     * Retrieves all entities in the table.
     */
    List<T> findAll();

    /**
     * Updates an existing entity's state.
     */
    boolean update(T entity);

    /**
     * Deletes an entity by primary key ID.
     */
    boolean deleteById(ID id);
}

