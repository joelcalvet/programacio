package org.example.model.daos;


import org.example.model.entities.Ninja;
import org.example.model.exceptions.DAOException;

import java.util.List;

public interface DAO <T>{

    T get(Long id) throws DAOException;

    List<T> getAll() throws DAOException;

    void save(T obj) throws DAOException;

    void update(Ninja obj) throws DAOException;

    void delete(Ninja obj) throws DAOException;

    //Tots els mètodes necessaris per interactuar en la BD

}
