package it.univaq.f4i.iw.ex.webmarket.data.dao;

import java.util.List;

import it.univaq.f4i.iw.ex.webmarket.data.model.Richiesta;
import it.univaq.f4i.iw.framework.data.DataException;



public interface RichiestaDAO {
    //crud
    Richiesta createRichiesta();

    Richiesta getRichiesta(int RichiestaOrdine_key) throws DataException;

    void storeRichiesta(Richiesta RichiestaOrdine) throws DataException;

    void deleteRichiestaOrdine(int richiesta_key) throws DataException;

    

    
    //altri metodi 
    List<Richiesta> getRichiesteByUtente(int utente_key) throws DataException;

    List<Richiesta> getRichiesteInAttesa() throws DataException;

    List<Richiesta> getRichiestePreseInCaricoConProposteByTecnico(int tecnico_key) throws DataException;

    List<Richiesta> getRichiestePreseInCaricoSenzaProposteByTecnico(int tecnico_key) throws DataException;

    boolean checkCompile(int richiesta_key) throws DataException;

}