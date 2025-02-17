package it.univaq.f4i.iw.ex.webmarket.data.dao.impl;

import it.univaq.f4i.iw.ex.webmarket.data.dao.CaratteristicaDAO;
import it.univaq.f4i.iw.ex.webmarket.data.dao.CaratteristicaRichiestaDAO;
import it.univaq.f4i.iw.ex.webmarket.data.dao.RichiestaDAO;
import it.univaq.f4i.iw.ex.webmarket.data.model.Caratteristica;
import it.univaq.f4i.iw.ex.webmarket.data.model.CaratteristicaRichiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.Richiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.proxy.CaratteristicaRichiestaProxy;
import it.univaq.f4i.iw.framework.data.DAO;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.data.DataItemProxy;
import it.univaq.f4i.iw.framework.data.DataLayer;
import it.univaq.f4i.iw.framework.data.OptimisticLockException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CaratteristicaRichiestaDAO_MySQL extends DAO implements CaratteristicaRichiestaDAO {

    private PreparedStatement sCR, sCByRichiesta, sRByCaratteristica, iCR, uCR;

    /**
     * Costruttore della classe.
     * 
     * @param d il DataLayer da utilizzare
     */
    public CaratteristicaRichiestaDAO_MySQL(DataLayer d) {
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
            sCR = connection.prepareStatement(
                "SELECT * FROM caratteristica_richiesta WHERE id = ?"
                );
            sCByRichiesta = connection.prepareStatement(
                "SELECT * FROM caratteristica_richiesta WHERE richiesta = ?"
                );
            sRByCaratteristica = connection.prepareStatement(
                "SELECT * FROM caratteristica_richiesta WHERE caratteristica = ?"
                );
            iCR = connection.prepareStatement(
                "INSERT INTO caratteristica_richiesta (richiesta, caratteristica, valore) VALUES(?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS
                );
            uCR = connection.prepareStatement(
                "UPDATE caratteristica_richiesta SET richiesta=?, caratteristica=?, valore=?, version=? WHERE id=? AND version=?"
                );
        } catch (SQLException ex) {
            throw new DataException("Error initializing data layer", ex);
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
            if (sCR != null) {
                sCR.close();
            }
            if (sCByRichiesta != null) {
                sCByRichiesta.close();
            }

            if (sRByCaratteristica != null) {
                sRByCaratteristica.close();
            }

            if (iCR != null) {
                iCR.close();
            }
            if (uCR != null) {
                uCR.close();
            }
        } catch (SQLException ex) {
            throw new DataException("Error while closing resources in CaratteristicaRichiestaDAO_MySQL", ex);
        } finally {
            super.destroy();
        }
    }
    
    /**
     * Crea una nuova istanza di CaratteristicaRichiesta.
     * 
     * @return una nuova istanza di CaratteristicaRichiestaProxy
     */
    @Override
    public CaratteristicaRichiesta createCR() {
        return new CaratteristicaRichiestaProxy(getDataLayer());
    }

    /**
     * Crea una CaratteristicaRichiestaProxy a partire da un ResultSet.
     * 
     * @param rs il ResultSet da cui creare la CaratteristicaRichiestaProxy
     * @return una nuova istanza di CaratteristicaRichiestaProxy
     * @throws DataException se si verifica un errore durante la creazione
     */
    private CaratteristicaRichiestaProxy createCR(ResultSet rs) throws DataException {
        try{
            CaratteristicaRichiestaProxy cr = (CaratteristicaRichiestaProxy) createCR();
            cr.setKey(rs.getInt("id"));
             RichiestaDAO richiestaDAO = (RichiestaDAO) dataLayer.getDAO(Richiesta.class);
             cr.setRichiesta(richiestaDAO.getRichiesta(rs.getInt("richiesta")));
            CaratteristicaDAO caratteristicaDAO = (CaratteristicaDAO) dataLayer.getDAO(Caratteristica.class);
            cr.setCaratteristica(caratteristicaDAO.getCaratteristica(rs.getInt("caratteristica")));
            cr.setValore(rs.getString("valore"));
            cr.setVersion(rs.getLong("version"));
            return cr;
        } catch (SQLException ex) {
            throw new DataException("Unable to create CaratteristicaRichiesta from ResultSet", ex);
        }
        
    }

    /**
     * Recupera una caratteristica richiesta dato il suo ID.
     * 
     * @param cr_key l'ID della caratteristica richiesta
     * @return la caratteristica richiesta corrispondente all'ID
     * @throws DataException se si verifica un errore durante il recupero
     */
     @Override
     public CaratteristicaRichiesta getCR(int cr_key) throws DataException {
         CaratteristicaRichiesta c = null;
          if (dataLayer.getCache().has(CaratteristicaRichiesta.class, cr_key)) {
             c = dataLayer.getCache().get(CaratteristicaRichiesta.class, cr_key);
          } else {
              try {
                  sCR.setInt(1, cr_key);
                  try (ResultSet rs = sCR.executeQuery()) {
                      if (rs.next()) {
                          c = createCR(rs);
                          dataLayer.getCache().add(CaratteristicaRichiesta.class, c);
                      }
                  }
              } catch (SQLException ex) {
                  throw new DataException("Unable to load CaratteristicaRichiesta by ID", ex);
              }
          }
          return c;
      }

     /**
     * Recupera le caratteristiche richieste associate a una richiesta.
     * 
     * @param richiesta_key l'ID della richiesta
     * @return una lista di caratteristiche richieste associate alla richiesta
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public List<CaratteristicaRichiesta> getCaratteristicheRichiestaByRichiesta(int richiesta_key) throws DataException {
        List<CaratteristicaRichiesta> caratteristiche = new ArrayList<>();
        try {
            sCByRichiesta.setInt(1, richiesta_key);
            try (ResultSet rs = sCByRichiesta.executeQuery()) {
                while (rs.next()) {
                    CaratteristicaRichiesta caratteristicheRichiesta = createCR(rs);
                    caratteristiche.add(caratteristicheRichiesta);
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load Caratteristiche by Richiesta", ex);
        }
        return caratteristiche;
    }
    
    
    /**
     * Memorizza una caratteristica richiesta nel database.
     * 
     * @param caratteristicaRichiesta la caratteristica richiesta da memorizzare
     * @throws DataException se si verifica un errore durante la memorizzazione
     */
    @Override
    public void storeCR(CaratteristicaRichiesta cr) throws DataException {
        try {
            if (cr.getKey() != null && cr.getKey() > 0) {
                if (cr instanceof CaratteristicaRichiestaProxy && !((CaratteristicaRichiestaProxy) cr).isModified()) {
                    return;
                }
                uCR.setInt(1, cr.getRichiesta().getKey());
                uCR.setInt(2, cr.getCaratteristica().getKey());
                uCR.setString(3, cr.getValore());
                long oldVersion = cr.getVersion();
                long versione = oldVersion + 1;
                uCR.setLong(4, versione);
                uCR.setInt(5, cr.getKey());
                uCR.setLong(6, oldVersion);
                if(uCR.executeUpdate() == 0){
                    throw new OptimisticLockException(cr);
                }else {
                    cr.setVersion(versione);
                }
            } else {
                iCR.setInt(1, cr.getRichiesta().getKey());
                iCR.setInt(2, cr.getCaratteristica().getKey());
                iCR.setString(3, cr.getValore());
                if (iCR.executeUpdate() == 1) {
                    try (ResultSet keys = iCR.getGeneratedKeys()) {
                        if (keys.next()) {
                            int key = keys.getInt(1);
                            cr.setKey(key);
                            dataLayer.getCache().add(CaratteristicaRichiesta.class, cr);
                        }
                    }
                }
            }
            if (cr instanceof DataItemProxy) {
                ((DataItemProxy) cr).setModified(false);
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to store CaratteristicaRichiesta", ex);
        }
    }


}