package it.univaq.f4i.iw.ex.webmarket.data.dao.impl;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.univaq.f4i.iw.ex.webmarket.data.dao.CategoriaDAO;
import it.univaq.f4i.iw.ex.webmarket.data.model.Categoria;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.proxy.CategoriaProxy;
import it.univaq.f4i.iw.framework.data.DAO;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.data.DataItemProxy;
import it.univaq.f4i.iw.framework.data.DataLayer;
import it.univaq.f4i.iw.framework.data.OptimisticLockException;

public class CategoriaDAO_MySQL extends DAO implements CategoriaDAO {

    private PreparedStatement sCategoriaByID; // Query per recuperare una categoria per ID
    private PreparedStatement iCategoria;    // Query per inserire una nuova categoria
    private PreparedStatement uCategoria;    // Query per aggiornare una categoria
    private PreparedStatement dCategoria;    // Query per eliminare una categoria

    /**
     * Costruttore della classe.
     * 
     * @param d il DataLayer da utilizzare
     */
    public CategoriaDAO_MySQL(DataLayer d) {
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

            sCategoriaByID = connection.prepareStatement("SELECT * FROM categoria WHERE ID=?");
            iCategoria = connection.prepareStatement(
                "INSERT INTO categoria(nome, padre) VALUES (?, ?)", 
                PreparedStatement.RETURN_GENERATED_KEYS
            );
            uCategoria = connection.prepareStatement(
                "UPDATE categoria SET nome=?, version=? WHERE ID=? AND version=?"
            );
            dCategoria = connection.prepareStatement("DELETE FROM categoria WHERE ID=?");

        } catch (SQLException e) {
            throw new DataException("Error initializing CategoriaDAO_MySQL", e);
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
            sCategoriaByID.close();
            iCategoria.close();
            uCategoria.close();
            dCategoria.close();
        } catch (SQLException ex) {
            throw new DataException("Error closing prepared statements", ex);
        }
        super.destroy();
    }

    /**
     * Crea una nuova istanza di Categoria.
     * 
     * @return una nuova istanza di CategoriaProxy
     */
    @Override
    public Categoria createCategoria() {
        return new CategoriaProxy(getDataLayer());
    }

    /**
     * Crea una CategoriaProxy a partire da un ResultSet.
     * 
     * @param rs il ResultSet da cui creare la CategoriaProxy
     * @return una nuova istanza di CategoriaProxy
     * @throws DataException se si verifica un errore durante la creazione
     */
    private Categoria createCategoria(ResultSet rs) throws DataException {
        try {
            CategoriaProxy cp = (CategoriaProxy) createCategoria();
            cp.setKey(rs.getInt("ID"));
            cp.setNome(rs.getString("nome"));
            cp.setPadre(rs.getInt("padre"));
            cp.setVersion(rs.getLong("version"));
            return cp;
        } catch (SQLException ex) {
            throw new DataException("Unable to create Categoria object from ResultSet", ex);
        }
    }

    /**
     * Recupera una categoria dato il suo ID.
     * 
     * @param categoria_key l'ID della categoria
     * @return la categoria corrispondente all'ID
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public Categoria getCategoria(int categoria_key) throws DataException {
        Categoria categoria = null;
        try {
            sCategoriaByID.setInt(1, categoria_key);
            try (ResultSet rs = sCategoriaByID.executeQuery()) {
                if (rs.next()) {
                    categoria = createCategoria(rs);
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load Categoria by ID", ex);
        }
        return categoria;
    }

    /**
     * Memorizza una categoria nel database.
     * 
     * @param categoria la categoria da memorizzare
     * @throws DataException se si verifica un errore durante la memorizzazione
     */
    @Override
    public void storeCategoria(Categoria categoria) throws DataException {
        try {
            if (categoria.getKey() != null && categoria.getKey() > 0) { // Aggiornamento
                if (categoria instanceof CategoriaProxy && !((CategoriaProxy) categoria).isModified()) {
                    return; // Nessuna modifica, salvataggio non necessario
                }
                uCategoria.setString(1, categoria.getNome());
                long oldVersion = categoria.getVersion();
                long newVersion = oldVersion + 1;
                uCategoria.setLong(2, newVersion);
                uCategoria.setInt(3, categoria.getKey());
                uCategoria.setLong(4, oldVersion);
                if (uCategoria.executeUpdate() == 0) {
                    throw new OptimisticLockException(categoria);
                } else {
                    categoria.setVersion(newVersion);
                }
            } else { // Inserimento
                iCategoria.setString(1, categoria.getNome());
                iCategoria.setInt(2, categoria.getPadre());
                if (iCategoria.executeUpdate() == 1) {
                    try (ResultSet keys = iCategoria.getGeneratedKeys()) {
                        if (keys.next()) {
                            categoria.setKey(keys.getInt(1));
                        }
                    }
                }
            }
            if (categoria instanceof DataItemProxy) {
                ((DataItemProxy) categoria).setModified(false);
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to store Categoria", ex);
        }
    }

    /**
     * Elimina una categoria dal database.
     * 
     * @param categoria la categoria da eliminare
     * @throws DataException se si verifica un errore durante l'eliminazione
     */
    @Override
    public void deleteCategoria(Categoria categoria) throws DataException {
        try {
            dCategoria.setInt(1, categoria.getKey());
            dCategoria.executeUpdate();
        } catch (SQLException ex) {
            throw new DataException("Unable to delete Categoria", ex);
        }
    }
}
