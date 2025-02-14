package it.univaq.f4i.iw.ex.webmarket.data.dao;
import it.univaq.f4i.iw.ex.webmarket.data.model.Proposta;
import it.univaq.f4i.iw.framework.data.DataException;
import java.util.List;

public interface PropostaDAO {

    Proposta createProposta();

    Proposta getProposta(int proposta_key) throws DataException;

    void storeProposta(Proposta proposta) throws DataException;

    List<Proposta> getProposteByOrdinante(int utente_key) throws DataException;

    List<Proposta> getProposteByTecnico(int tecnico_key) throws DataException;

    List<Proposta> getProposteByRichiesta(int richiesta_id) throws DataException; 

    boolean notificaP_T(int tecnicoId) throws DataException;
    
    boolean notificaP_O(int ordinanteId) throws DataException;

}
