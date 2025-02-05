package it.univaq.f4i.iw.ex.webmarket.data.dao;

import it.univaq.f4i.iw.ex.webmarket.data.model.Recensione;
import it.univaq.f4i.iw.framework.data.DataException;

/**
 * Data Access Object (DAO) interface per le entità {@link Recensione}.
 * Definisce le operazioni CRUD e altre operazioni per la gestione delle recensioni nel datastore.
 */
public interface RecensioneDAO {

   
    Recensione createRecensione();

    
    Recensione getRecensione(int recensione_key) throws DataException;

   
    void storeRecensione(Recensione recensione) throws DataException;

    void deleteRecensione(int recensione_key) throws DataException;
    //metodo che serve per prendere una recensione dato un ordinante e un tecnico
    Recensione getRecensioneByOrdinanteTecnico(int ordinante_id ,int tecnico_id) throws DataException;

    //serve una query dal db per prendere la media delle recensioni di ogni tecnico
    //poi andrebbe messa in ordine descrescente e mostrata in home
}
