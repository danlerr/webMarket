package it.univaq.f4i.iw.ex.webmarket.data.dao;

import java.util.List;

import it.univaq.f4i.iw.ex.webmarket.data.model.Categoria;
import it.univaq.f4i.iw.framework.data.DataException;


public interface CategoriaDAO {

    Categoria createCategoria();

    Categoria getCategoria(int categoria_key) throws DataException;

    void storeCategoria(Categoria categoria) throws DataException;

    void deleteCategoria(Categoria categoria) throws DataException;
    
    List<Categoria> getAllCategorie() throws DataException;

    List<Categoria> getCategorieByPadre(int padre) throws DataException;

    List<Categoria> getMainCategorie() throws DataException;
    
}


