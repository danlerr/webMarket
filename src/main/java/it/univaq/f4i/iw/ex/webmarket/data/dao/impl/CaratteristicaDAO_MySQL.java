package it.univaq.f4i.iw.ex.webmarket.data.dao.impl;

import it.univaq.f4i.iw.ex.webmarket.data.dao.CaratteristicaDAO;
import it.univaq.f4i.iw.ex.webmarket.data.model.Caratteristica;
import it.univaq.f4i.iw.ex.webmarket.data.model.Categoria;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.proxy.CaratteristicaProxy;
import it.univaq.f4i.iw.framework.data.DAO;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.data.DataLayer;
import it.univaq.f4i.iw.framework.data.OptimisticLockException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class CaratteristicaDAO_MySQL extends DAO implements CaratteristicaDAO {

    private PreparedStatement sCaratteristica, sCaratteristiche, iCaratteristica, uCaratteristica, dCaratteristica, sCaratteristicaByCategoria;

    public CaratteristicaDAO_MySQL(DataLayer d) {
        super(d);
    }

    @Override
    public void init() throws DataException {
        try {
            super.init();

            sCaratteristica = connection.prepareStatement(
                "SELECT * FROM caratteristica WHERE id = ?"
                );
            sCaratteristiche = connection.prepareStatement(
                "SELECT * FROM caratteristica"
                );
            iCaratteristica = connection.prepareStatement(
                "INSERT INTO caratteristica (nome, categoria_id) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS
                );
            uCaratteristica = connection.prepareStatement(
                "UPDATE caratteristica SET nome=?, categoria_id=?, version=? WHERE id=? AND version=?"
                );
            dCaratteristica = connection.prepareStatement(
                "DELETE FROM caratteristica WHERE id=?"
                );
            sCaratteristicaByCategoria = connection.prepareStatement(
                "SELECT * FROM caratteristica WHERE categoria_id=?"
                );

        } catch (SQLException ex) {
            throw new DataException("Error initializing caratteristica data layer", ex);
        }
    }

    @Override
    public void destroy() throws DataException {
        try {
            sCaratteristica.close();
            sCaratteristiche.close();
            iCaratteristica.close();
            uCaratteristica.close();
            dCaratteristica.close();
            sCaratteristicaByCategoria.close();

        } catch (SQLException ex) {
            //
        }
        super.destroy();
    }

/**
     * Crea una nuova istanza di Caratteristica.
     * 
     * @return una nuova istanza di CaratteristicaProxy
     */
    @Override
    public Caratteristica createCaratteristica() {
    
         return new CaratteristicaProxy(getDataLayer()); 

    }

    /**
     * Crea una CaratteristicaProxy a partire da un ResultSet.
     * 
     * @param results il ResultSet da cui creare la CaratteristicaProxy
     * @return una nuova istanza di CaratteristicaProxy
     * @throws DataException se si verifica un errore durante la creazione
     */
    private CaratteristicaProxy createCaratteristica(ResultSet results) throws DataException {
        try {
            CaratteristicaProxy cProxy = (CaratteristicaProxy) createCaratteristica();
            cProxy.setKey(results.getInt("id"));
            cProxy.setNome(results.getString("nome"));
            cProxy.setVersion(results.getLong("version"));
             int categoriaId = results.getInt("categoria_id");
            Categoria categoria = ((ApplicationDataLayer) getDataLayer()).getCategoriaDAO().getCategoria(categoriaId);
            cProxy.setCategoria(categoria);

            return cProxy;
        } catch (SQLException ex) {
            throw new DataException("Unable to create caratteristica object from ResultSet", ex);
        }
    }

    /**
     * Recupera una caratteristica dato il suo ID.
     * 
     * @param caratteristica_key l'ID della caratteristica
     * @return la caratteristica corrispondente all'ID
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public Caratteristica getCaratteristica(int caratteristica_key) throws DataException {
  
        Caratteristica caratteristica = null;
        if (dataLayer.getCache().has(Caratteristica.class, caratteristica_key)) {
            caratteristica = dataLayer.getCache().get(Caratteristica.class, caratteristica_key);
        } else {
            try {
                sCaratteristica.setInt(1, caratteristica_key);
                try (ResultSet rs = sCaratteristica.executeQuery()) {
                    if (rs.next()) {
                        caratteristica = createCaratteristica(rs);
                        dataLayer.getCache().add(Caratteristica.class, caratteristica);
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load caratteristica by ID", ex);
            }
        }
        return caratteristica;
  
   }

   /**
     * Memorizza una caratteristica nel database.
     * 
     * @param caratteristica la caratteristica da memorizzare
     * @throws DataException se si verifica un errore durante la memorizzazione
     */
    @Override
    public void storeCaratteratica(Caratteristica caratteristica) throws DataException {
        try {
            if (caratteristica.getKey() != null && caratteristica.getKey() > 0) {
                if (caratteristica instanceof CaratteristicaProxy && !((CaratteristicaProxy) caratteristica).isModified()) {
                    return;
                }
                // Update
                uCaratteristica.setString(1, caratteristica.getNome());
                uCaratteristica.setInt(2, caratteristica.getCategoria().getKey());
                long oldVersion = caratteristica.getVersion();
                long versione = oldVersion + 1;
                uCaratteristica.setLong(3, versione);
                uCaratteristica.setInt(4, caratteristica.getKey());
                uCaratteristica.setLong(5, oldVersion);
                if(uCaratteristica.executeUpdate() == 0){
                    throw new OptimisticLockException(caratteristica);
                }else {
                    caratteristica.setVersion(versione);
                }
            } else {
                // Insert
                    iCaratteristica.setString(1, caratteristica.getNome());
                    iCaratteristica.setInt(2, caratteristica.getCategoria().getKey());
                if (iCaratteristica.executeUpdate() == 1) {
                    try (ResultSet keys = iCaratteristica.getGeneratedKeys()) {
                        if (keys.next()) {
                            caratteristica.setKey(keys.getInt(1));
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to store caratteristica", ex);
        }
    }

    /**
     * Elimina una caratteristica dal database.
     * 
     * @param caratteristica_key l'ID della caratteristica da eliminare
     * @throws DataException se si verifica un errore durante l'eliminazione
     */
    @Override
    public void deleteCaratteristica(int caratteristica_key) throws DataException {
        try {
            dCaratteristica.setInt(1, caratteristica_key);
            dCaratteristica.executeUpdate();
        } catch (SQLException ex) {
            throw new DataException("Unable to delete caratteristica", ex);
        }
    }
    /**
     * Recupera le caratteristiche associate a una categoria.
     * 
     * @param categoria l'ID della categoria
     * @return una lista di caratteristiche associate alla categoria
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public List<Caratteristica> getCaratteristicheByCategoria(int categoria) throws DataException {
        List<Caratteristica> result = new ArrayList<>();
    try {
        sCaratteristicaByCategoria.setInt(1, categoria);
        try (ResultSet rs = sCaratteristicaByCategoria.executeQuery()) {
            while (rs.next()) {
                result.add(createCaratteristica(rs));
            }
        }
    } catch (SQLException ex) {
        throw new DataException("Unable to load caratteristiche by categoria ID", ex);
    }
    return result;
    }
}