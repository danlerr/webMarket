package it.univaq.f4i.iw.ex.webmarket.data.dao.impl;

import it.univaq.f4i.iw.ex.webmarket.data.dao.RichiestaDAO;
import it.univaq.f4i.iw.ex.webmarket.data.model.Categoria;
import it.univaq.f4i.iw.ex.webmarket.data.model.Richiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.Utente;
import it.univaq.f4i.iw.ex.webmarket.data.model.StatoRichiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.proxy.RichiestaProxy;
import it.univaq.f4i.iw.framework.data.DAO;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.data.DataItemProxy;
import it.univaq.f4i.iw.framework.data.DataLayer;
import it.univaq.f4i.iw.framework.data.OptimisticLockException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class RichiestaDAO_MySQL extends DAO implements RichiestaDAO {

    private PreparedStatement sRichiestaByID;
    private PreparedStatement sRichiesteByUtente;
    private PreparedStatement iRichiesta;
    private PreparedStatement uRichiesta;
    private PreparedStatement sRichiesteSenzaProposte;
    private PreparedStatement sRichiestePreseInCaricoConProposteByTecnico;
    private PreparedStatement sRichiesteInAttesa; 
    private PreparedStatement sCheckCompile;


    
    public RichiestaDAO_MySQL(DataLayer d) {
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

            // PreparedStatement per recuperare una Richiesta per ID
            sRichiestaByID = connection.prepareStatement(
                "SELECT * FROM richiesta WHERE id = ?"
                );
            // PreparedStatment per recuperare le richieste dato userId
            sRichiesteByUtente = connection.prepareStatement(
                "SELECT * FROM richiesta WHERE ordinante = ? ORDER BY data DESC");
            // PreparedStatement per inserire una nuova Richiesta
            iRichiesta = connection.prepareStatement(
                "INSERT INTO richiesta (note, stato, data, codice_richiesta, ordinante, tecnico, categoria, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS
            );

            // PreparedStatement per aggiornare una Richiesta esistente
            uRichiesta = connection.prepareStatement(
                "UPDATE richiesta SET note=?, stato=?, data=?, codice_richiesta=?, ordinante=?, tecnico=?, categoria=?, version=? " +
                "WHERE id=? AND version=?"
            );
            sRichiesteSenzaProposte = connection.prepareStatement(
                "SELECT r.id, r.note, r.stato, r.data, r.codice_richiesta, r.ordinante, r.tecnico, r.categoria " +
                "FROM richiesta r " +
                "WHERE r.stato = ? AND r.tecnico = ? " +
                "  AND NOT EXISTS (SELECT 1 FROM proposta p " +
                "                  WHERE p.richiesta_id = r.id AND p.stato <> 'RIFIUTATO') " +
                "ORDER BY r.data ASC"
            );

            
            // PreparedStatement per recuperare le richieste prese in carico da un tecnico
            sRichiestePreseInCaricoConProposteByTecnico = connection.prepareStatement(
                "SELECT r.id, r.note, r.stato, r.data, r.codice_richiesta, r.ordinante, r.tecnico, r.categoria " +
                "FROM richiesta r " +
                "WHERE r.stato = ? AND r.tecnico = ? " +
                "  AND EXISTS (SELECT 1 FROM proposta p " +
                "              WHERE p.richiesta_id = r.id AND p.stato <> 'RIFIUTATO') " +
                "ORDER BY r.data ASC"
            );

            // PreparedStatement per recuperare le richieste in attesa 
            sRichiesteInAttesa = connection.prepareStatement("SELECT * FROM richiesta WHERE stato = ?");
            // Prepared Statement che conta quante proposte non rifiutate esistono per una certa richiesta
            sCheckCompile = connection.prepareStatement(
                "SELECT COUNT(*) AS cnt "+
                "FROM proposta "+ 
                "WHERE richiesta_id = ? "+
                "AND stato <> 'RIFIUTATO'"
            );

        } catch (SQLException ex) {
            throw new DataException("Errore durante l'inizializzazione del RichiestaDAO", ex);
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
            if (sRichiestaByID != null && !sRichiestaByID.isClosed()) {
                sRichiestaByID.close();
            }
            if (iRichiesta != null && !iRichiesta.isClosed()) {
                iRichiesta.close();
            }
            if (uRichiesta != null && !uRichiesta.isClosed()) {
                uRichiesta.close();
            }
            if (sRichiesteSenzaProposte != null && !sRichiesteSenzaProposte.isClosed()) {
                sRichiesteSenzaProposte.close();
            }
            if (sRichiesteByUtente != null && !sRichiesteByUtente.isClosed()) {
                sRichiesteByUtente.close();
            }
            if (sRichiestePreseInCaricoConProposteByTecnico != null && !sRichiestePreseInCaricoConProposteByTecnico.isClosed()) {
                sRichiestePreseInCaricoConProposteByTecnico.close();
            }
            if (sRichiesteInAttesa != null && !sRichiesteInAttesa.isClosed()) { 
                sRichiesteInAttesa.close();
            }
            if (sCheckCompile != null && !sCheckCompile.isClosed()) {
                sCheckCompile.close();
            }
            
        } catch (SQLException ex) {
            throw new DataException("Errore durante la chiusura del RichiestaDAO", ex);
        }
        super.destroy();
    }


    /**
     * Crea una nuova istanza di Richiesta.
     *
     * @return una nuova istanza di RichiestaProxy
     */
    @Override
    public Richiesta createRichiesta() {
        return new RichiestaProxy(getDataLayer());
    }

    /**
     * Crea una RichiestaProxy a partire da un ResultSet.
     *
     * @param rs il ResultSet da cui creare la RichiestaProxy
     * @return una nuova istanza di RichiestaProxy
     * @throws DataException se si verifica un errore durante la creazione
     */
    private RichiestaProxy createRichiesta(ResultSet rs) throws DataException {
        try {
            RichiestaProxy richiesta = (RichiestaProxy) createRichiesta();
            richiesta.setKey(rs.getInt("id"));
            richiesta.setNote(rs.getString("note"));
            richiesta.setStato(StatoRichiesta.valueOf(rs.getString("stato")));
            richiesta.setData(rs.getDate("data"));
            richiesta.setCodiceRichiesta(rs.getString("codice_richiesta"));
            richiesta.setVersion(rs.getLong("version"));

            int tecnicoId = rs.getInt("tecnico");
            Utente tecnico = ((ApplicationDataLayer) getDataLayer()).getUtenteDAO().getUtente(tecnicoId);
            richiesta.setTecnico(tecnico);

            int categoriaId = rs.getInt("categoria");
            Categoria categoria = ((ApplicationDataLayer) getDataLayer()).getCategoriaDAO().getCategoria(categoriaId);
            richiesta.setCategoria(categoria);

            int utenteId = rs.getInt("ordinante");
            Utente utente = ((ApplicationDataLayer) getDataLayer()).getUtenteDAO().getUtente(utenteId);
            richiesta.setOrdinante(utente);

            return richiesta;
        } catch (SQLException ex) {
            throw new DataException("Impossibile creare l'oggetto Richiesta dal ResultSet", ex);
        }
    }

    /**
     * Recupera una Richiesta dato il suo ID.
     *
     * @param richiesta_key l'ID della Richiesta
     * @return la Richiesta corrispondente all'ID
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public Richiesta getRichiesta(int richiesta_key) throws DataException {
        Richiesta richiesta = null;
        if (dataLayer.getCache().has(Richiesta.class, richiesta_key)) {
            richiesta = dataLayer.getCache().get(Richiesta.class, richiesta_key);
        } else {
            try {
                sRichiestaByID.setInt(1, richiesta_key);
                try (ResultSet rs = sRichiestaByID.executeQuery()) {
                    if (rs.next()) {
                        richiesta = createRichiesta(rs);
                        dataLayer.getCache().add(Richiesta.class, richiesta);
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Impossibile caricare la Richiesta per ID", ex);
            }
        }
        return richiesta;
    }

    /**
     * Memorizza una Richiesta nel database.
     *
     * @param richiesta la Richiesta da memorizzare
     * @throws DataException se si verifica un errore durante la memorizzazione
     */
    @Override
    public void storeRichiesta(Richiesta richiesta) throws DataException {
        try {
            if (richiesta.getKey() != null && richiesta.getKey() > 0) {
                // UPDATE - Richiesta esistente
                if (richiesta instanceof RichiestaProxy && !((RichiestaProxy) richiesta).isModified()) {
                    return; // Nessuna modifica
                }
    
                uRichiesta.setString(1, richiesta.getNote());
                uRichiesta.setString(2, richiesta.getStato().name());
                uRichiesta.setDate(3, new java.sql.Date(richiesta.getData().getTime()));
                uRichiesta.setString(4, richiesta.getCodiceRichiesta());
                uRichiesta.setInt(5, richiesta.getOrdinante().getKey());
    
                // Gestione del tecnico NULL (UPDATE)
                if (richiesta.getTecnico() != null) {
                    uRichiesta.setInt(6, richiesta.getTecnico().getKey());
                } else {
                    uRichiesta.setNull(6, java.sql.Types.INTEGER);
                }
    
                uRichiesta.setInt(7, richiesta.getCategoria().getKey());
                long oldVersion = richiesta.getVersion();
                long newVersion = oldVersion + 1;
                uRichiesta.setLong(8, newVersion);
                uRichiesta.setInt(9, richiesta.getKey());
                uRichiesta.setLong(10, oldVersion);
    
                int affectedRows = uRichiesta.executeUpdate();
                if (affectedRows == 0) {
                    throw new OptimisticLockException(richiesta);
                }
                richiesta.setVersion(newVersion);
    
            } else {
                // INSERT - Nuova richiesta
                iRichiesta.setString(1, richiesta.getNote());
                iRichiesta.setString(2, richiesta.getStato().name());
                iRichiesta.setDate(3, new java.sql.Date(richiesta.getData().getTime()));
                iRichiesta.setString(4, richiesta.getCodiceRichiesta());
                iRichiesta.setInt(5, richiesta.getOrdinante().getKey());
    
                // Gestione del tecnico NULL (INSERT)
                if (richiesta.getTecnico() != null) {
                    iRichiesta.setInt(6, richiesta.getTecnico().getKey());
                } else {
                    iRichiesta.setNull(6, java.sql.Types.INTEGER);
                }
    
                iRichiesta.setInt(7, richiesta.getCategoria().getKey());
                iRichiesta.setLong(8, 1L); // Versione iniziale
    
                if (iRichiesta.executeUpdate() == 1) {
                    try (ResultSet keys = iRichiesta.getGeneratedKeys()) {
                        if (keys.next()) {
                            int key = keys.getInt(1);
                            richiesta.setKey(key);
                            dataLayer.getCache().add(Richiesta.class, richiesta);
                        }
                    }
                }
            }
    
            if (richiesta instanceof DataItemProxy) {
                ((DataItemProxy) richiesta).setModified(false);
            }
        } catch (SQLException ex) {
            throw new DataException("Impossibile memorizzare la Richiesta", ex);
        }
    }

    
    //ritorna tutte le richieste di uno specifico utente
    @Override
    public List<Richiesta> getRichiesteByUtente(int utente_key) throws DataException {
        List<Richiesta> result = new ArrayList<>();
        try {
            sRichiesteByUtente.setInt(1, utente_key);
            try (ResultSet rs = sRichiesteByUtente.executeQuery()) {
                while (rs.next()) {
                    result.add(getRichiesta(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load RichiesteOrdine by Utente", ex);
        }
        return result;
    }
     @Override
    public List<Richiesta> getRichiestePreseInCaricoConProposteByTecnico(int tecnico_key) throws DataException {
        List<Richiesta> result = new ArrayList<>();
    
        try {
            sRichiestePreseInCaricoConProposteByTecnico.setString(1, StatoRichiesta.PRESA_IN_CARICO.name());
            sRichiestePreseInCaricoConProposteByTecnico.setInt(2, tecnico_key);
            try (ResultSet rs = sRichiestePreseInCaricoConProposteByTecnico.executeQuery()) {
                while (rs.next()) {
                    result.add(getRichiesta(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load Richieste prese in carico by Tecnico", ex);
        }
        return result;
    }
        
     /**
     * Recupera le richieste inoltrate che devono ancora essere prese in carico.
     * 
     * @return una lista di richieste inoltrate
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public List<Richiesta> getRichiesteInAttesa() throws DataException {
        List<Richiesta> result = new ArrayList<>();
        try {
            sRichiesteInAttesa.setString(1, StatoRichiesta.IN_ATTESA.name());
            try (ResultSet rs = sRichiesteInAttesa.executeQuery()) {
                while (rs.next()) {
                    result.add(getRichiesta(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load RichiesteOrdine Inoltrate", ex);
        }
        return result;
    }
    /**
     * Recupera le richieste senza proposte per un tecnico specifico.
     * 
     * @param tecnico_key l'ID del tecnico
     * @return una lista di richieste non evase
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public List<Richiesta> getRichiestePreseInCaricoSenzaProposteByTecnico(int tecnico_key) throws DataException {

        List<Richiesta> result = new ArrayList<>();
        try {
            sRichiesteSenzaProposte.setString(1, StatoRichiesta.PRESA_IN_CARICO.name());
            sRichiesteSenzaProposte.setInt(2, tecnico_key);
            try (ResultSet rs = sRichiesteSenzaProposte.executeQuery()) {
                while (rs.next()) {
                    result.add(getRichiesta(rs.getInt("id")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load RichiesteOrdine non evase", ex);
        }
        return result;
    }


    /**
     * Elimina una richiesta dal database.
     * 
     * @param richiesta_key l'ID della richiesta da eliminare
     * @throws DataException se si verifica un errore durante l'eliminazione
     */
    @Override
    public void deleteRichiestaOrdine(int richiesta_key) throws DataException {
      try {
        PreparedStatement dRichiestaOrdine = connection.prepareStatement("DELETE FROM richiesta_ordine WHERE id=?");
        dRichiestaOrdine.setInt(1, richiesta_key);
        dRichiestaOrdine.executeUpdate();
        dataLayer.getCache().delete(Richiesta.class, richiesta_key); 
        dRichiestaOrdine.close();
    } catch (SQLException ex) {
        throw new DataException("Unable to delete RichiestaOrdine", ex);
    }
}

    @Override
    public boolean checkCompile(int richiesta_key) throws DataException {
        try {
            // Prepariamo i parametri per la query
            sCheckCompile.setInt(1, richiesta_key);
            
            // Eseguiamo la query
            try (ResultSet rs = sCheckCompile.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("cnt");
                    // Se count == 0, non ci sono proposte "attive" (cioè != RIFIUTATO)
                    return (count == 0);
                } else {
                    // Caso inatteso: nessun risultato
                    return false;
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Errore durante l'esecuzione di checkCompile", ex);
        }
    }



}

