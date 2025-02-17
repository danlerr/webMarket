package it.univaq.f4i.iw.ex.webmarket.data.dao.impl;

import it.univaq.f4i.iw.ex.webmarket.data.dao.RecensioneDAO;
import it.univaq.f4i.iw.ex.webmarket.data.model.Recensione;
import it.univaq.f4i.iw.ex.webmarket.data.model.Utente;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.proxy.RecensioneProxy;
import it.univaq.f4i.iw.framework.data.DAO;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.data.DataItemProxy;
import it.univaq.f4i.iw.framework.data.DataLayer;
import it.univaq.f4i.iw.framework.data.OptimisticLockException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap; 

import java.util.Map;

/**
 * Implementazione della classe RecensioneDAO utilizzando MySQL.
 */
public class RecensioneDAO_MySQL extends DAO implements RecensioneDAO {

    private PreparedStatement sRecensioneByID;
    private PreparedStatement iRecensione;
    private PreparedStatement uRecensione;
    private PreparedStatement dRecensione;
    private PreparedStatement sRecensioneByOrdinanteTecnico;
    private PreparedStatement sMedieRecensioniTecnici; 



    /**
     * Costruttore della classe.
     *
     * @param d il DataLayer da utilizzare
     */
    public RecensioneDAO_MySQL(DataLayer d) {
        super(d);
    }

    /**
     * Inizializza le PreparedStatement per le operazioni CRUD.
     *
     * @throws DataException se si verifica un errore durante l'inizializzazione
     */
    @Override
    public void init() throws DataException {
        try {
            super.init();

            // PreparedStatement per recuperare una Recensione per ID
            sRecensioneByID = connection.prepareStatement(
                "SELECT * FROM recensione WHERE id = ?"
                );

            // PreparedStatement per inserire una nuova Recensione
            iRecensione = connection.prepareStatement(
                "INSERT INTO recensione (valore, autore, destinatario, version) " +
                "VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS
            );

            // PreparedStatement per aggiornare una Recensione esistente
            uRecensione = connection.prepareStatement(
                "UPDATE recensione SET valore=?, autore=?, destinatario=?, version=? " +
                "WHERE id=? AND version=?"
            );
            // PreparedStatement per recuperare una Recensione dato ordinante e tecnico
            sRecensioneByOrdinanteTecnico = connection.prepareStatement(
                "SELECT * FROM recensione WHERE autore = ? AND destinatario = ?"
            );
            sMedieRecensioniTecnici = connection.prepareStatement(
                "SELECT r.destinatario AS tecnico_id, AVG(r.valore) AS media_voti " +
                "FROM recensione r " +
                "GROUP BY r.destinatario " +
                "HAVING AVG(r.valore) >= 4 " +
                "ORDER BY media_voti DESC" // 
            );

            // PreparedStatement per eliminare una Recensione
            dRecensione = connection.prepareStatement("DELETE FROM recensione WHERE id=?");
        } catch (SQLException ex) {
            throw new DataException("Errore durante l'inizializzazione del RecensioneDAO", ex);
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
            if (sRecensioneByID != null && !sRecensioneByID.isClosed()) {
                sRecensioneByID.close();
            }
            if (iRecensione != null && !iRecensione.isClosed()) {
                iRecensione.close();
            }
            if (uRecensione != null && !uRecensione.isClosed()) {
                uRecensione.close();
            }
            if (dRecensione != null && !dRecensione.isClosed()) {
                dRecensione.close();
            }
            if (sRecensioneByOrdinanteTecnico != null && !sRecensioneByOrdinanteTecnico.isClosed()) {
                sRecensioneByOrdinanteTecnico.close();
            }
            if (sMedieRecensioniTecnici != null && !sMedieRecensioniTecnici.isClosed()) {
                sMedieRecensioniTecnici.close();
            }
        } catch (SQLException ex) {
            throw new DataException("Errore durante la chiusura del RecensioneDAO", ex);
        }
        super.destroy();
    }

    /**
     * Crea una nuova istanza di Recensione.
     *
     * @return una nuova istanza di RecensioneProxy
     */
    @Override
    public Recensione createRecensione() {
        return new RecensioneProxy(getDataLayer());
    }

    /**
     * Crea una RecensioneProxy a partire da un ResultSet.
     *
     * @param rs il ResultSet da cui creare la RecensioneProxy
     * @return una nuova istanza di RecensioneProxy
     * @throws DataException se si verifica un errore durante la creazione
     */
    private RecensioneProxy createRecensione(ResultSet rs) throws DataException {
        try {
            RecensioneProxy recensione = (RecensioneProxy) createRecensione();
            recensione.setKey(rs.getInt("id"));
            recensione.setValore(rs.getInt("valore"));
            recensione.setVersion(rs.getLong("version"));
            int autoreId = rs.getInt("autore");
            
            Utente autore = ((ApplicationDataLayer) getDataLayer()).getUtenteDAO().getUtente(autoreId);
            recensione.setAutore(autore);
            int destinatarioId = rs.getInt("destinatario");
            Utente destinatario = ((ApplicationDataLayer) getDataLayer()).getUtenteDAO().getUtente(destinatarioId);
            recensione.setDestinatario(destinatario);

            return recensione;
        } catch (SQLException ex) {
            throw new DataException("Impossibile creare l'oggetto Recensione dal ResultSet", ex);
        }
    }

    /**
     * Recupera una Recensione dato il suo ID.
     *
     * @param recensione_key l'ID della Recensione
     * @return la Recensione corrispondente all'ID
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public Recensione getRecensione(int recensione_key) throws DataException {
        Recensione recensione = null;
        if (dataLayer.getCache().has(Recensione.class, recensione_key)) {
            recensione = dataLayer.getCache().get(Recensione.class, recensione_key);
        } else {
            try {
                sRecensioneByID.setInt(1, recensione_key);
                try (ResultSet rs = sRecensioneByID.executeQuery()) {
                    if (rs.next()) {
                        recensione = createRecensione(rs);
                        dataLayer.getCache().add(Recensione.class, recensione);
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Impossibile caricare la Recensione per ID", ex);
            }
        }
        return recensione;
    }

    /**
     * Memorizza una Recensione nel database.
     *
     * @param recensione la Recensione da memorizzare
     * @throws DataException se si verifica un errore durante la memorizzazione
     */
    @Override
    public void storeRecensione(Recensione recensione) throws DataException {
        try {
            if (recensione.getKey() != null && recensione.getKey() > 0) {
                if (recensione instanceof RecensioneProxy && !((RecensioneProxy) recensione).isModified()) {
                    return; // Nessuna modifica da salvare
                }

                // Aggiornamento della Recensione esistente
                uRecensione.setInt(1, recensione.getValore());
                uRecensione.setInt(2, recensione.getAutore().getKey());
                uRecensione.setInt(3, recensione.getDestinatario().getKey());
                uRecensione.setLong(4, recensione.getVersion() + 1); // Incrementa la versione
                uRecensione.setInt(5, recensione.getKey());
                uRecensione.setLong(6, recensione.getVersion());

                int affectedRows = uRecensione.executeUpdate();
                if (affectedRows == 0) {
                    throw new OptimisticLockException(recensione);
                } else {
                    recensione.setVersion(recensione.getVersion() + 1);
                }
            } else {
                // Inserimento di una nuova Recensione
                iRecensione.setInt(1, recensione.getValore());
                iRecensione.setInt(2, recensione.getAutore().getKey());
                iRecensione.setInt(3, recensione.getDestinatario().getKey());
                iRecensione.setLong(4, 1L); // Version iniziale

                if (iRecensione.executeUpdate() == 1) {
                    try (ResultSet keys = iRecensione.getGeneratedKeys()) {
                        if (keys.next()) {
                            int key = keys.getInt(1);
                            recensione.setKey(key);
                            dataLayer.getCache().add(Recensione.class, recensione);
                        }
                    }
                }
            }

            if (recensione instanceof DataItemProxy) {
                ((DataItemProxy) recensione).setModified(false);
            }
        } catch (SQLException ex) {
            throw new DataException("Impossibile memorizzare la Recensione", ex);
        }
    }

    /**
     * Elimina una Recensione dal database.
     *
     * @param recensione_key l'ID della Recensione da eliminare
     * @throws DataException se si verifica un errore durante l'eliminazione
     */
    @Override
    public void deleteRecensione(int recensione_key) throws DataException {
        try {
            dRecensione.setInt(1, recensione_key);
            dRecensione.executeUpdate();
            dataLayer.getCache().delete(Recensione.class, recensione_key);
        } catch (SQLException ex) {
            throw new DataException("Impossibile eliminare la Recensione", ex);
        }
    }

    @Override
    public Recensione getRecensioneByOrdinanteTecnico(int ordinante_id, int tecnico_id) throws DataException {
        Recensione recensione = null;
        try {
            sRecensioneByOrdinanteTecnico.setInt(1, ordinante_id);
            sRecensioneByOrdinanteTecnico.setInt(2, tecnico_id);
            try (ResultSet rs = sRecensioneByOrdinanteTecnico.executeQuery()) {
                if (rs.next()) {
                    recensione = createRecensione(rs);
                    //aggiunta recensione alla cache
                    dataLayer.getCache().add(Recensione.class, recensione);
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Impossibile caricare la Recensione per ordinante e tecnico", ex);
        }
        return recensione;
    }

    @Override
    /**
     * Calcola la media dei voti delle recensioni per ogni tecnico.
     *
     * @return Una mappa dove la chiave è l'ID del tecnico e il valore è la media dei voti.
     * @throws DataException se si verifica un errore durante l'accesso al database.
     */
    public Map<Integer, Double> getMedieRecensioniTecnici() throws DataException {
        Map<Integer, Double> medie = new LinkedHashMap<>(); // <-- Usa LinkedHashMap
        try (ResultSet rs = sMedieRecensioniTecnici.executeQuery()) {
            while (rs.next()) {
                int tecnicoId = rs.getInt("tecnico_id");
                double mediaVoti = rs.getDouble("media_voti");
                medie.put(tecnicoId, mediaVoti);
            }
        } catch (SQLException ex) {
            throw new DataException("Impossibile calcolare le medie delle recensioni per i tecnici", ex);
        }
        return medie;
    }
}
