package it.univaq.f4i.iw.ex.webmarket.data.dao;

import it.univaq.f4i.iw.ex.webmarket.data.model.Ordine;
import it.univaq.f4i.iw.framework.data.DataException;
import java.util.List;
 

public interface OrdineDAO {

    Ordine createOrdine();

    Ordine getOrdine(int ordine_key) throws DataException;

    List<Ordine> getOrdiniByOrdinante(int utente_key) throws DataException;

 
    List<Ordine> getOrdiniByTecnico(int tecnico_key) throws DataException;

   

    void storeOrdine(Ordine ordine) throws DataException;

    void deleteOrdine(int ordine_key) throws DataException;

    boolean notificaO_O(int tecnicoId) throws DataException;
    
    boolean notificaO_T(int ordinanteId) throws DataException;

}