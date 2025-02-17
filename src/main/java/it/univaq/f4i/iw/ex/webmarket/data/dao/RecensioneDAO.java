package it.univaq.f4i.iw.ex.webmarket.data.dao;

import java.util.Map;

import it.univaq.f4i.iw.ex.webmarket.data.model.Recensione;
import it.univaq.f4i.iw.framework.data.DataException;

public interface RecensioneDAO {

   
    Recensione createRecensione();

    
    Recensione getRecensione(int recensione_key) throws DataException;

   
    void storeRecensione(Recensione recensione) throws DataException;

    void deleteRecensione(int recensione_key) throws DataException;
    
    
    //metodo che serve per prendere una recensione dato un ordinante e un tecnico
    Recensione getRecensioneByOrdinanteTecnico(int ordinante_id ,int tecnico_id) throws DataException;

    
    
    Map<Integer, Double> getMedieRecensioniTecnici() throws DataException;
}
