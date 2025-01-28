package it.univaq.f4i.iw.ex.webmarket.data.dao;

import it.univaq.f4i.iw.ex.webmarket.data.model.Categoria;
import it.univaq.f4i.iw.framework.data.DataException;


public interface CategoriaDAO {

//---------------------------------CRUD-------------------------------
    Categoria createCategoria();

    Categoria getCategoria(int categoria_key) throws DataException;

    void storeCategoria(Categoria categoria) throws DataException;

    void deleteCategoria(Categoria categoria) throws DataException;
    
//----------------------------------------------------------------

}


