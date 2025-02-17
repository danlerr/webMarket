package it.univaq.f4i.iw.ex.webmarket.data.dao.impl;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import it.univaq.f4i.iw.ex.webmarket.data.dao.CategoriaDAO;
import it.univaq.f4i.iw.ex.webmarket.data.model.Categoria;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.proxy.CategoriaProxy;
import it.univaq.f4i.iw.framework.data.DAO;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.data.DataItemProxy;
import it.univaq.f4i.iw.framework.data.DataLayer;

public class CategoriaDAO_MySQL extends DAO implements CategoriaDAO {

    private PreparedStatement sCategoria, iCategoria, uCategoria, dCategoria, sCategorieByPadre, sMainCategorie;
    
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

            sCategoria = connection.prepareStatement(
                "SELECT * FROM categoria WHERE id=?"
                );
            iCategoria = connection.prepareStatement(
                "INSERT INTO categoria(nome, padre) VALUES (?, ?)", 
                PreparedStatement.RETURN_GENERATED_KEYS
                );
            uCategoria = connection.prepareStatement(
                "UPDATE categoria SET nome=?, version=? WHERE id=? AND version=?"
                );
            dCategoria = connection.prepareStatement(
                "DELETE FROM categoria WHERE id=?"
                );
            sCategorieByPadre = connection.prepareStatement(
                "SELECT * FROM categoria WHERE padre = ?"
                );
            sMainCategorie = connection.prepareStatement(
                "SELECT * FROM categoria WHERE padre IS NULL"
                );

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
            sCategoria.close();
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
     * @param results il ResultSet da cui creare la CategoriaProxy
     * @return una nuova istanza di CategoriaProxy
     * @throws DataException se si verifica un errore durante la creazione
     */
    private Categoria createCategoria(ResultSet results) throws DataException {
        try {
            CategoriaProxy cproxy = (CategoriaProxy) createCategoria();
            cproxy.setKey(results.getInt("id"));
            cproxy.setNome(results.getString("nome"));
            cproxy.setPadre(results.getInt("padre"));
            cproxy.setVersion(results.getLong("version"));
            return cproxy;
        } catch (SQLException exc) {
            throw new DataException("Unable to create Categoria object from ResultSet", exc);
        }
    }

    /**
     * Recupera categoria dato il suo ID.
     * 
     * @param categoria_key l'ID della categoria
     * @return la categoria corrispondente all'ID
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public Categoria getCategoria(int categoria_key) throws DataException {
        Categoria categoria = null;
        try {
            sCategoria.setInt(1, categoria_key);
            try (ResultSet rs = sCategoria.executeQuery()) {
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
            // Logica per l'inserimento della categoria
            if (categoria.getKey() == null || categoria.getKey() <= 0) { // Se la categoria non ha una chiave, è un inserimento
                iCategoria.setString(1, categoria.getNome());
                iCategoria.setInt(2, categoria.getPadre());

                // Esegui l'inserimento
                if (iCategoria.executeUpdate() == 1) {
                    try (ResultSet keys = iCategoria.getGeneratedKeys()) {
                        if (keys.next()) {
                            categoria.setKey(keys.getInt(1)); // Imposta la chiave generata
                        }
                    }
                }
            }
            
            // Se la categoria è un DataItemProxy, segna come non modificata
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

    /**
     * Recupera tutte le categorie dal db
     * 
     * @return lista di tutte le categorie
     * @throws DataException errore durante il retrieve 
     */
    @Override
    public List<Categoria> getAllCategorie() throws DataException {
    List<Categoria> result = new ArrayList<>();
        try {
            try (ResultSet rs = sCategoria.executeQuery()) {
                while (rs.next()) {
                    result.add(getCategoria(rs.getInt("ID")));
                }
            }
            return result;
        } catch (SQLException ex) {
            throw new DataException("Error loading all categories", ex);
        }
    }

    @Override
    public List<Categoria> getCategorieByPadre(int padre) throws DataException {
        List<Categoria> result = new ArrayList<>();
        try {
            sCategorieByPadre.setInt(1, padre);
            try (ResultSet rs = sCategorieByPadre.executeQuery()) {
                while (rs.next()) {
                    result.add(createCategoria(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Impossibile caricare le categorie dato l'id del padre", ex);
        }
        return result;
        }

    @Override
    public List<Categoria> getMainCategorie() throws DataException {
        List<Categoria> result = new ArrayList<>();
        try {
            try (ResultSet r = sMainCategorie.executeQuery()) {
                while (r.next()) {
                    result.add(createCategoria(r));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Impossibile caricare le categorie principali", ex);
        }
        return result;
    }
}
