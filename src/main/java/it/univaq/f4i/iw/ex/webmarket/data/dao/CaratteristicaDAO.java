package it.univaq.f4i.iw.ex.webmarket.data.dao;

import it.univaq.f4i.iw.ex.webmarket.data.model.Caratteristica;
import it.univaq.f4i.iw.framework.data.DataException;


public interface CaratteristicaDAO {

//---------------------------------CRUD-------------------------------
    Caratteristica createCaratteristica();

    Caratteristica getCaratteristica(int Caratteristica_key) throws DataException;

    void storeCaratteratica(Caratteristica caratteristica) throws DataException;

    void deleteCaratteristica(int caratteristica_key) throws DataException;

//----------------------------------------------------------------
}
