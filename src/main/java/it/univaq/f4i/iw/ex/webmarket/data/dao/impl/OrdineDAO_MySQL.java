package it.univaq.f4i.iw.ex.webmarket.data.dao.impl;

import it.univaq.f4i.iw.ex.webmarket.data.dao.OrdineDAO;
import it.univaq.f4i.iw.ex.webmarket.data.dao.PropostaDAO;
import it.univaq.f4i.iw.ex.webmarket.data.model.Ordine;
import it.univaq.f4i.iw.ex.webmarket.data.model.Proposta;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.StatoOrdine;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.proxy.OrdineProxy;
import it.univaq.f4i.iw.framework.data.DAO;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.data.DataItemProxy;
import it.univaq.f4i.iw.framework.data.DataLayer;
import it.univaq.f4i.iw.framework.data.OptimisticLockException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrdineDAO_MySQL extends DAO implements OrdineDAO {
    
    private PreparedStatement sOrdine, iOrdine, uOrdine, dOrdine, sOrdiniByOrdinante, sOrdiniByTecnico, ordiniNot_T, ordiniNot_O;

    /**
     * Costruttore della classe.
     * 
     * @param d il DataLayer da utilizzare
     */
    public OrdineDAO_MySQL(DataLayer d) {
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
            sOrdine = connection.prepareStatement(
                "SELECT * FROM ordine WHERE id = ?"
                );
            iOrdine = connection.prepareStatement(
                "INSERT INTO ordine (stato, proposta_id, data) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS
                );
            uOrdine = connection.prepareStatement(
                "UPDATE ordine SET stato=?, proposta_id=? , data=?, version=? WHERE id=? AND version=?"
                );
            dOrdine = connection.prepareStatement(
                "DELETE FROM ordine WHERE id=?"
                );
            sOrdiniByOrdinante = connection.prepareStatement(
                "SELECT o.* FROM ordine o JOIN proposta p ON o.proposta_id = p.id JOIN richiesta r ON p.richiesta_id = r.id WHERE r.ordinante = ?  ORDER BY CASE WHEN o.stato = 'IN_ATTESA' THEN 1 ELSE 2 END, o.data DESC"
                );
            sOrdiniByTecnico = connection.prepareStatement(
                "SELECT o.* FROM ordine o JOIN proposta p ON o.proposta_id = p.id JOIN richiesta r ON p.richiesta_id = r.id WHERE r.tecnico = ? ORDER BY CASE WHEN (o.stato = 'RESPINTO_NON_CONFORME' OR o.stato = 'RESPINTO_NON_FUNZIONANTE') THEN 1 ELSE 2 END, o.data DESC"
                );
            ordiniNot_T = connection.prepareStatement(
                 "SELECT EXISTS( SELECT 1 FROM ordine o JOIN proposta p ON o.proposta_id = p.id JOIN richiesta r ON p.richiesta_id = r.id WHERE (o.stato = 'RESPINTO_NON_CONFORME' OR o.stato = 'RESPINTO_NON_FUNZIONANTE') AND r.tecnico = ?) AS notifica_o;"
                );
            ordiniNot_O = connection.prepareStatement(
                 "SELECT EXISTS( SELECT 1 FROM ordine o JOIN proposta p ON o.proposta_id = p.id JOIN richiesta r ON p.richiesta_id = r.id WHERE (o.stato = 'IN_ATTESA') AND r.ordinante = ?) AS notifica_o;"
                );

                
        } catch (SQLException ex) {
            throw new DataException("Error initializing ordine data layer", ex);
        }
    }

    /**
     * Chiude le PreparedStatement.
     * 
     * @throws DataException se si verifica un errore durante la chiusura
     */
    @Override
    public void destroy() throws DataException {
        try {
            sOrdine.close();
            iOrdine.close();
            uOrdine.close();
            dOrdine.close();
            sOrdiniByOrdinante.close();
            sOrdiniByTecnico.close();
            ordiniNot_T.close();
            ordiniNot_O.close();
        } catch (SQLException ex) {
            
        }
        super.destroy();
    }

    /**
     * Crea una nuova istanza di Ordine.
     * 
     * @return una nuova istanza di OrdineProxy
     */
    @Override
    public Ordine createOrdine() {
        return new OrdineProxy(getDataLayer());
    }

    /**
     * Crea una OrdineProxy a partire da un ResultSet.
     * 
     * @param rs il ResultSet da cui creare la OrdineProxy
     * @return una nuova istanza di OrdineProxy
     * @throws DataException se si verifica un errore durante la creazione
     */
    private OrdineProxy createOrdine(ResultSet rs) throws DataException {
        try {
            OrdineProxy o = (OrdineProxy) createOrdine();
             int id = rs.getInt("id");
             o.setKey(id);
             o.setStato(StatoOrdine.valueOf(rs.getString("stato")));
              PropostaDAO propostaAcquistoDAO = (PropostaDAO) dataLayer.getDAO(Proposta.class);
             o.setProposta(propostaAcquistoDAO.getProposta(rs.getInt("proposta_id")));
             o.setData(rs.getDate("data"));
             o.setVersion(rs.getLong("version"));
            return o;
        } catch (SQLException ex) {
            throw new DataException("Unable to create ordine object from ResultSet", ex);
        }
    }

    /**
     * Recupera un ordine dato il suo ID.
     * 
     * @param ordine_key l'ID dell'ordine
     * @return l'ordine corrispondente all'ID
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public Ordine getOrdine(int ordine_key) throws DataException {
        Ordine o = null;
        if (dataLayer.getCache().has(Ordine.class, ordine_key)) {
            o = dataLayer.getCache().get(Ordine.class, ordine_key);
        } else {
            try {
                sOrdine.setInt(1, ordine_key);
                try (ResultSet result = sOrdine.executeQuery()) {
                    if (result.next()) {
                        o = createOrdine(result);
                        dataLayer.getCache().add(Ordine.class, o);
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load ordine by ID", ex);
            }
        }
        return o;
    }
    
    @Override
    public void storeOrdine(Ordine ordine) throws DataException {
        try {
            if (ordine.getKey() != null && ordine.getKey() > 0) {
                 // Se l'ordine è un proxy e non è stato modificato, salta l'aggiornamento
                if (ordine instanceof OrdineProxy && !((OrdineProxy) ordine).isModified()) {
                    return;
                }
               

                uOrdine.setString(1, ordine.getStato().toString());
                uOrdine.setInt(2, ordine.getProposta().getKey());
                uOrdine.setDate(3, new java.sql.Date(ordine.getData().getTime()));
                long oldVersion = ordine.getVersion();
                long versione = oldVersion + 1;
                uOrdine.setLong(4, versione);
                uOrdine.setInt(5, ordine.getKey());
                uOrdine.setLong(6, oldVersion);
                if(uOrdine.executeUpdate() == 0){
                    throw new OptimisticLockException(ordine);
                }else {
                    ordine.setVersion(versione);
                }
            } else {
                // Inserisce un nuovo ordine nel database
                iOrdine.setString(1, ordine.getStato().toString());
                iOrdine.setInt(2, ordine.getProposta().getKey());
                iOrdine.setDate(3, new java.sql.Date(ordine.getData().getTime()));
                if (iOrdine.executeUpdate() == 1) {
                    try (ResultSet keys = iOrdine.getGeneratedKeys()) {
                        if (keys.next()) {
                            int key = keys.getInt(1);
                            ordine.setKey(key);
                            dataLayer.getCache().add(Ordine.class, ordine);
                        }
                    }
                }
            }
            if (ordine instanceof DataItemProxy) {
                ((DataItemProxy) ordine).setModified(false);
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to store ordine", ex);
        }
    }

    @Override
    public void deleteOrdine(int ordine_key) throws DataException {
        try {
            dOrdine.setInt(1, ordine_key);
            int rowsAffected = dOrdine.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataException("No ordine found with the given ID.");
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to delete ordine", ex);
        }
    }

    /**
     * Recupera gli ordini associati a un utente.
     * 
     * @param utente_key l'ID dell'utente
     * @return una lista di ordini associati all'utente
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public List<Ordine> getOrdiniByOrdinante(int utente_key) throws DataException {
        List<Ordine> ordini = new ArrayList<>();
        try {
            sOrdiniByOrdinante.setInt(1, utente_key);
            try (ResultSet rs = sOrdiniByOrdinante.executeQuery()) {
                while (rs.next()) {
                    ordini.add(createOrdine(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load ordini by utente ID", ex);
        }
        return ordini;
    }

    /**
     * Recupera gli ordini associati a un tecnico.
     * 
     * @param tecnico_key l'ID del tecnico
     * @return una lista di ordini associati al tecnico
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public List<Ordine> getOrdiniByTecnico(int tecnico_key) throws DataException {
        List<Ordine> ordini = new ArrayList<>();
        try {
            sOrdiniByTecnico.setInt(1, tecnico_key);
            try (ResultSet rs = sOrdiniByTecnico.executeQuery()) {
                while (rs.next()) {
                    ordini.add(createOrdine(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load ordini by tecnico ID", ex);
        }
        return ordini;
    }

    public boolean notificaO_T(int tecnicoId) throws DataException {

        try {
            ordiniNot_T.setInt(1, tecnicoId);
            try (ResultSet rs = ordiniNot_T.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("notifica_o");
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Impossibile controllore le notifiche relative agli ordini(tec)", ex);
        }
        return false;
    }
    
    /**
     * Verifica se ci sono ordini da notificare per un ordinante.
     * 
     * @param ordinanteId 
     * @return true se ci sono ordini da notificare
     * @throws DataException se si verifica un errore 
     */
     @Override
    public boolean notificaO_O(int ordinanteId) throws DataException {

        try {
            ordiniNot_O.setInt(1, ordinanteId);
            try (ResultSet rs = ordiniNot_O.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("notifica_o");
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Impossibile controllore le notifiche relative agli ordini(ord)", ex);
        }
        return false;
    }
}