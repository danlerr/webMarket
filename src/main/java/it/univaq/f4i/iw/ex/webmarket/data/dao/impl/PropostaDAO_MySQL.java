package it.univaq.f4i.iw.ex.webmarket.data.dao.impl;

import it.univaq.f4i.iw.ex.webmarket.data.dao.PropostaDAO;
import it.univaq.f4i.iw.ex.webmarket.data.model.Proposta;
import it.univaq.f4i.iw.ex.webmarket.data.model.Richiesta;
import it.univaq.f4i.iw.ex.webmarket.data.dao.RichiestaDAO;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.StatoProposta;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.proxy.PropostaProxy;
import it.univaq.f4i.iw.framework.data.DAO;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.data.DataLayer;
import it.univaq.f4i.iw.framework.data.OptimisticLockException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class PropostaDAO_MySQL extends DAO implements PropostaDAO {

    private PreparedStatement sProposta, iProposta, uProposta, dProposta, sProposteByOrdinante, sProposteByTecnico,sProposteByRichiesta, proposteNotT, proposteNotO;

    public PropostaDAO_MySQL(DataLayer d) {
        super(d);
    }

    /**
     * Inizializza le PreparedStatement.
     * 
     * @throws DataException se si verifica un errore durante l'inizializzazione
     */
    @Override
    public void init() throws DataException {
        try {
            super.init();

            sProposta = connection.prepareStatement(
                "SELECT * FROM proposta WHERE id = ?"
                );

            iProposta = connection.prepareStatement(
                "INSERT INTO proposta (produttore, prodotto, codice, codice_prodotto, prezzo, URL, note, stato, data, motivazione, richiesta_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS
                );
            
            uProposta = connection.prepareStatement(
                "UPDATE proposta SET produttore=?, prodotto=?, codice=?, codice_prodotto=?, prezzo=?, URL=?, note=?, stato=?, data=?, motivazione=?, richiesta_id=?, version=? WHERE id=? AND version=?"
                );

            dProposta = connection.prepareStatement(
                "DELETE FROM proposta WHERE id=?"
                );
            sProposteByOrdinante = connection.prepareStatement(
                "SELECT p.* FROM proposta p JOIN richiesta r ON p.richiesta_id = r.id WHERE r.ordinante = ? ORDER BY CASE WHEN (p.stato = 'IN_ATTESA') THEN 1 ELSE 2 END"
                );
            sProposteByTecnico = connection.prepareStatement(
                "SELECT p.* FROM proposta p JOIN richiesta r ON p.richiesta_id = r.id WHERE r.tecnico = ? ORDER BY CASE WHEN (p.stato = 'ACCETTATO') THEN 1 ELSE 2 END"
                );
            sProposteByRichiesta = connection.prepareStatement(
                "SELECT * FROM proposta WHERE richiesta_id = ?"
                );
            proposteNotT = connection.prepareStatement( 
                "SELECT EXISTS(" +
                " SELECT 1 " +
                " FROM proposta p " +
                " JOIN richiesta r ON p.richiesta_id = r.id " +
                " WHERE (p.stato = 'ACCETTATO') " +
                " AND r.tecnico = ?" +
                ") AS notifica_p;"
                );
                
            proposteNotO = connection.prepareStatement( 
                "SELECT EXISTS(" +
                " SELECT 1 " +
                " FROM proposta p " +
                " JOIN richiesta r ON p.richiesta_id = r.id " +
                " WHERE (p.stato = 'IN_ATTESA') " +
                " AND r.ordinante = ?" +
                ") AS notifica_p;"
                );
                

        } catch (SQLException ex) {
            throw new DataException("Errore durante l'inizializzazione del data layer per le proposte d'acquisto", ex);
        }
    }

    
    @Override
    public void destroy() throws DataException {
        try {
            sProposta.close();
            iProposta.close();
            uProposta.close();
            dProposta.close();
            sProposteByOrdinante.close();
            sProposteByTecnico.close();
            sProposteByRichiesta.close();
            proposteNotT.close();
            proposteNotO.close();
        } catch (SQLException ex) {
        }
        super.destroy();
    }

    @Override
    public Proposta createProposta() {
        return new PropostaProxy(getDataLayer());
    }

    /**
     * Crea una PropostaAcquistoProxy a partire da un ResultSet.
     * 
     * @param rs il ResultSet da cui creare la PropostaAcquistoProxy
     * @return una nuova istanza di PropostaAcquistoProxy
     * @throws DataException se si verifica un errore durante la creazione
     */
    private PropostaProxy createProposta(ResultSet results) throws DataException {
        try {
            PropostaProxy p = (PropostaProxy) createProposta();
            p.setKey(results.getInt("id"));
            p.setProduttore(results.getString("produttore"));
            p.setProdotto(results.getString("prodotto"));
            p.setCodice(results.getString("codice"));
            p.setCodiceProdotto(results.getString("codice_prodotto"));
            p.setPrezzo(results.getFloat("prezzo"));
            p.setUrl(results.getString("URL"));
            p.setNote(results.getString("note"));
            p.setStatoProposta(StatoProposta.valueOf(results.getString("stato")));
            p.setData(results.getDate("data"));
            p.setMotivazione(results.getString("motivazione"));
            p.setVersion(results.getLong("version"));
            RichiestaDAO richiestaOrdineDAO = (RichiestaDAO) dataLayer.getDAO(Richiesta.class);
            p.setRichiesta(richiestaOrdineDAO.getRichiesta(results.getInt("richiesta_id")));
            return p;
        } catch (SQLException ex) {
            throw new DataException("Impossibile creare l'oggetto proposta d'acquisto dal ResultSet", ex);
        }
    }

    /**
     * Recupera una proposta d'acquisto dato il suo ID.
     * 
     * @param proposta_key l'ID della proposta d'acquisto
     * @return la proposta d'acquisto corrispondente all'ID
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public Proposta getProposta(int proposta_key) throws DataException {
        Proposta p = null;
        if (dataLayer.getCache().has(Proposta.class, proposta_key)) {
            p = dataLayer.getCache().get(Proposta.class, proposta_key);
        } else {
            try {
                sProposta.setInt(1, proposta_key);
                try (ResultSet result = sProposta.executeQuery()) {
                    if (result.next()) {
                        p = createProposta(result);
                        dataLayer.getCache().add(Proposta.class, p);
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Impossibile caricare la proposta d'acquisto tramite ID", ex);
            }
        }
        return p;
    }

    /**
     * Memorizza una proposta d'acquisto nel database.
     * 
     * @param proposta la proposta d'acquisto da memorizzare
     * @throws DataException se si verifica un errore durante la memorizzazione
     */
    @Override
    public void storeProposta(Proposta proposta) throws DataException {
        try {
            if (proposta.getKey() != null && proposta.getKey() > 0) {

                if (proposta instanceof PropostaProxy && !((PropostaProxy) proposta).isModified()) {
                    return;
                }

                uProposta.setString(1, proposta.getProduttore());
                uProposta.setString(2, proposta.getProdotto());
                uProposta.setString(3, proposta.getCodice());
                uProposta.setString(4, proposta.getCodiceProdotto());
                uProposta.setDouble(5, proposta.getPrezzo());
                uProposta.setString(6, proposta.getUrl());
                uProposta.setString(7, proposta.getNote());
                uProposta.setString(8, proposta.getStatoProposta().toString());
                uProposta.setDate(9, Date.valueOf(LocalDate.now()));
                uProposta.setString(10, proposta.getMotivazione());
                uProposta.setInt(11, proposta.getRichiesta().getKey());
                long oldVersion = proposta.getVersion();
                long versione = oldVersion + 1;
                uProposta.setLong(12, versione);
                uProposta.setInt(13, proposta.getKey());
                uProposta.setLong(14, oldVersion);
                if(uProposta.executeUpdate() == 0){
                    throw new OptimisticLockException(proposta);
                }else {
                    proposta.setVersion(versione);
                }
            } else {

                iProposta.setString(1, proposta.getProduttore());
                iProposta.setString(2, proposta.getProdotto());
                iProposta.setString(3, proposta.getCodice());
                iProposta.setString(4, proposta.getCodiceProdotto());
                iProposta.setDouble(5, proposta.getPrezzo());
                iProposta.setString(6, proposta.getUrl());
                iProposta.setString(7, proposta.getNote());
                iProposta.setString(8, proposta.getStatoProposta().toString());
                iProposta.setDate(9, Date.valueOf(LocalDate.now()));
                iProposta.setString(10, proposta.getMotivazione());
                iProposta.setInt(11, proposta.getRichiesta().getKey());
                if (iProposta.executeUpdate() == 1) {
                    try (ResultSet keys = iProposta.getGeneratedKeys()) {
                        if (keys.next()) {
                            proposta.setKey(keys.getInt(1));
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Impossibile salvare la proposta d'acquisto", ex);
        }
    }

    @Override
    public List<Proposta> getProposteByOrdinante(int utente_key) throws DataException {
           List<Proposta> proposte = new ArrayList<>();
    try {
        sProposteByOrdinante.setInt(1, utente_key);
        try (ResultSet result = sProposteByOrdinante.executeQuery()) {
            while (result.next()) {
                proposte.add(createProposta(result));
            }
        }
    } catch (SQLException ex) {
        throw new DataException("Impossibile caricare le proposte d'acquisto per l'utente specificato", ex);
    }
    return proposte;
    }

    @Override
    public List<Proposta> getProposteByTecnico(int tecnico_key) throws DataException {
        List<Proposta> proposte = new ArrayList<>();
        try {
            sProposteByTecnico.setInt(1, tecnico_key);
            try (ResultSet result = sProposteByTecnico.executeQuery()) {
                while (result.next()) {
                    proposte.add(createProposta(result));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Impossibile caricare le proposte d'acquisto per il tecnico specificato", ex);
        }
        return proposte;
        }
        /**
     * Recupera le proposte d'acquisto associate a una richiesta.
     * 
     * @param richiesta_id l'ID della richiesta
     * @return una lista di proposte d'acquisto associate alla richiesta
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public List<Proposta> getProposteByRichiesta(int richiesta_id) throws DataException {
        List<Proposta> proposte = new ArrayList<>();
        try {
            sProposteByRichiesta.setInt(1, richiesta_id);
            try (ResultSet rs = sProposteByRichiesta.executeQuery()) {
                while (rs.next()) {
                    proposte.add(createProposta(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Impossibile caricare le proposte d'acquisto per la richiesta specificata", ex);
        }
        return proposte;
    }
    /**
     * Verifica se ci sono proposte da notificare per un tecnico.
     * 
     * @param tecnicoId 
     * @return true se ci sono proposte d'acquisto da notificare 
     * @throws DataException se si verifica un errore 
     */
    @Override
    public boolean notificaP_T(int tecnicoId) throws DataException {
        try {
            proposteNotT.setInt(1, tecnicoId);

            try (ResultSet rs = proposteNotT.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("notifica_p");
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Impossibile controllore le notifiche relative alle proposte(tec)", ex);
        }
        return false;
    }
    
    /**
     * Verifica se ci sono proposte da notificare per un ordinante.
     * 
     * @param ordinanteId 
     * @return true se ci sono proposte d'acquisto da notificare
     * @throws DataException se si verifica un errore 
     */
    @Override
    public boolean notificaP_O(int ordinanteId) throws DataException {
        try {
            proposteNotO.setInt(1, ordinanteId);

            try (ResultSet rs = proposteNotO.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("notifica_p");
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Impossibile controllore le notifiche relative alle proposte(ord)", ex);
        }
        return false;
    }
}
