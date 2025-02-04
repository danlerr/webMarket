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
    private PreparedStatement dRichiesta;
    private PreparedStatement sRichiestePreseInCaricoByTecnico;
    private PreparedStatement sRichiesteInAttesaByTecnico; 

    
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
                "SELECT * FROM Richiesta WHERE ID = ?"
                );
            // PreparedStatment per recuperare le richieste dato userId
            sRichiesteByUtente = connection.prepareStatement(
                "SELECT * FROM Richiesta WHERE utente = ?"
                );
            // PreparedStatement per inserire una nuova Richiesta
            iRichiesta = connection.prepareStatement(
                "INSERT INTO Richiesta (note, stato, data, codice_richiesta, utente, tecnico, categoria_id, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS
            );

            // PreparedStatement per aggiornare una Richiesta esistente
            uRichiesta = connection.prepareStatement(
                "UPDATE Richiesta SET note=?, stato=?, data=?, codice_richiesta=?, utente=?, tecnico=?, categoria_id=?, version=? " +
                "WHERE ID=? AND version=?"
            );

            // PreparedStatement per eliminare una Richiesta
            dRichiesta = connection.prepareStatement("DELETE FROM Richiesta WHERE ID=?");

            // PreparedStatement per recuperare le richieste prese in carico da un tecnico
            sRichiestePreseInCaricoByTecnico = connection.prepareStatement(
                "SELECT r.ID FROM Richiesta r " +
                "JOIN StatoRichiesta sr ON r.stato = sr.ID " +
                "WHERE sr.nome = 'PRESE_IN_CARICO' AND r.tecnico = ?"
            );

            // PreparedStatement per recuperare le richieste in attesa da un tecnico
            sRichiesteInAttesaByTecnico = connection.prepareStatement(  // <-- Aggiunto
                "SELECT r.ID FROM Richiesta r " +
                "JOIN StatoRichiesta sr ON r.stato = sr.ID " +
                "WHERE sr.nome = 'IN_ATTESA' AND r.tecnico = ?"
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
            if (dRichiesta != null && !dRichiesta.isClosed()) {
                dRichiesta.close();
            }
            if (sRichiesteByUtente != null && !sRichiesteByUtente.isClosed()) {
                sRichiesteByUtente.close();
            }
            if (sRichiestePreseInCaricoByTecnico != null && !sRichiestePreseInCaricoByTecnico.isClosed()) {
                sRichiestePreseInCaricoByTecnico.close();
            }
            if (sRichiesteInAttesaByTecnico != null && !sRichiesteInAttesaByTecnico.isClosed()) { // <-- Aggiunto
                sRichiesteInAttesaByTecnico.close();
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
            richiesta.setKey(rs.getInt("ID"));
            richiesta.setNote(rs.getString("note"));
            richiesta.setStato(StatoRichiesta.valueOf(rs.getString("stato")));
            richiesta.setData(rs.getDate("data"));
            richiesta.setCodiceRichiesta(rs.getString("codice_richiesta"));
            richiesta.setVersion(rs.getLong("version"));

            int tecnicoId = rs.getInt("tecnico");
            Utente tecnico = ((ApplicationDataLayer) getDataLayer()).getUtenteDAO().getUtente(tecnicoId);
            richiesta.setTecnico(tecnico);

            int categoriaId = rs.getInt("categoria_id");
            Categoria categoria = ((ApplicationDataLayer) getDataLayer()).getCategoriaDAO().getCategoria(categoriaId);
            richiesta.setCategoria(categoria);

            int utenteId = rs.getInt("utente");
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
                if (richiesta instanceof RichiestaProxy && !((RichiestaProxy) richiesta).isModified()) {
                    return; // Nessuna modifica da salvare
                }

                // Aggiornamento della Richiesta esistente
                uRichiesta.setString(1, richiesta.getNote());
                uRichiesta.setString(2, richiesta.getStato().name());
                uRichiesta.setDate(3, new java.sql.Date(richiesta.getData().getTime()));
                uRichiesta.setString(4, richiesta.getCodiceRichiesta());
                uRichiesta.setInt(5, richiesta.getOrdinante().getKey());
                uRichiesta.setInt(6, richiesta.getTecnico().getKey());
                uRichiesta.setInt(7, richiesta.getCategoria().getKey());
                long oldVersion = richiesta.getVersion();
                long newVersion = oldVersion + 1;
                uRichiesta.setLong(8, newVersion);
                uRichiesta.setInt(9, richiesta.getKey());
                uRichiesta.setLong(10, oldVersion);

                int affectedRows = uRichiesta.executeUpdate();
                if (affectedRows == 0) {
                    throw new OptimisticLockException(richiesta);
                } else {
                    richiesta.setVersion(newVersion);
                }
            } else {
                // Inserimento di una nuova Richiesta
                iRichiesta.setString(1, richiesta.getNote());
                iRichiesta.setString(2, richiesta.getStato().name());
                iRichiesta.setDate(3, new java.sql.Date(richiesta.getData().getTime()));
                iRichiesta.setString(4, richiesta.getCodiceRichiesta());
                iRichiesta.setInt(5, richiesta.getOrdinante().getKey());
                iRichiesta.setInt(6, richiesta.getTecnico().getKey());
                iRichiesta.setInt(7, richiesta.getCategoria().getKey());
                iRichiesta.setLong(8, 1L); // Version iniziale

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

    /**
     * Elimina una Richiesta dal database.
     *
     * @param richiesta_key l'ID della Richiesta da eliminare
     * @throws DataException se si verifica un errore durante l'eliminazione
     */
    @Override
    public void deleteRichiesta(int richiesta_key) throws DataException {
        try {
            dRichiesta.setInt(1, richiesta_key);
            dRichiesta.executeUpdate();
            dataLayer.getCache().delete(Richiesta.class, richiesta_key);
        } catch (SQLException ex) {
            throw new DataException("Impossibile eliminare la Richiesta", ex);
        }
    }

    @Override
    public List<Richiesta> getRichiesteByUtente(int utente_key) throws DataException {
        List<Richiesta> result = new ArrayList<>();
        try {
            sRichiesteByUtente.setInt(1, utente_key);
            try (ResultSet rs = sRichiesteByUtente.executeQuery()) {
                while (rs.next()) {
                    result.add(getRichiesta(rs.getInt("ID")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load RichiesteOrdine by Utente", ex);
        }
        return result;
    }
     @Override
    public List<Richiesta> getRichiestePreseInCaricoByTecnico(int tecnico_key) throws DataException {
        List<Richiesta> result = new ArrayList<>();
    
        try {
            sRichiestePreseInCaricoByTecnico.setInt(1, tecnico_key);
            try (ResultSet rs = sRichiestePreseInCaricoByTecnico.executeQuery()) {
                while (rs.next()) {
                    result.add(getRichiesta(rs.getInt("ID")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load Richieste prese in carico by Tecnico", ex);
        }
        return result;
    }
        
    @Override
    public List<Richiesta> getRichiesteInAttesaByTecnico(int tecnico_key) throws DataException {
        List<Richiesta> result = new ArrayList<>();

        try {
            sRichiesteInAttesaByTecnico.setInt(1, tecnico_key);
            try (ResultSet rs = sRichiesteInAttesaByTecnico.executeQuery()) {
                while (rs.next()) {
                    result.add(getRichiesta(rs.getInt("ID")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load Richieste in attesa by Tecnico", ex);
        }
        return result;
    }
}
