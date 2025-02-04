package it.univaq.f4i.iw.ex.webmarket.data.dao;

import it.univaq.f4i.iw.ex.webmarket.data.model.Proposta;
import it.univaq.f4i.iw.framework.data.DataException;
//import java.util.List;

public interface PropostaDAO {

    Proposta createProposta();

    Proposta getProposta(int proposta_key) throws DataException;

    void storeProposta(Proposta proposta) throws DataException;

    //List<PropostaAcquisto> getProposteByUtente(int utente_key) throws DataException;

    //List<PropostaAcquisto> getProposteByTecnico(int tecnico_key) throws DataException;

    //List<PropostaAcquisto> getProposteByOrdine(int ordine_key) throws DataException;

    //List<PropostaAcquisto> getAllProposteAcquisto() throws DataException;

    //List<PropostaAcquisto> getProposteAcquistoByRichiesta(int richiesta_id) throws DataException; 

    //void inviaPropostaAcquisto(PropostaAcquisto proposta) throws DataException;
    
    //boolean notificaProposte(int tecnicoId) throws DataException;
    
    //boolean notificaProposteOrd(int ordinanteId) throws DataException;
}
