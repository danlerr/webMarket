package it.univaq.f4i.iw.ex.webmarket.data.dao;

import java.util.List;

import it.univaq.f4i.iw.ex.webmarket.data.model.Richiesta;
import it.univaq.f4i.iw.framework.data.DataException;



public interface RichiestaDAO {

    Richiesta createRichiesta();

    Richiesta getRichiesta(int RichiestaOrdine_key) throws DataException;


    void storeRichiesta(Richiesta RichiestaOrdine) throws DataException;

    List<Richiesta> getRichiesteByUtente(int utente_key) throws DataException;

    void deleteRichiesta(int richiesta_key) throws DataException;
    
  

}