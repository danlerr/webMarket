package it.univaq.f4i.iw.framework.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.CaratteristicaDAO_MySQL;
import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.RichiestaDAO_MySQL;
import it.univaq.f4i.iw.ex.webmarket.data.model.Caratteristica;
import it.univaq.f4i.iw.ex.webmarket.data.model.Richiesta;

/**
 *
 * @author Giuseppe Della Penna
 */
/**
 * La classe DataLayer gestisce l'accesso ai dati, fornendo una connessione al database e
 * l'accesso ai vari DAO (Data Access Object) per interagire con le entità di dati.
 * 
 * Oltre alla gestione delle connessioni e dei DAO, la classe gestisce anche una cache 
 * in memoria per migliorare le prestazioni nelle operazioni di lettura dei dati.
 * Implementa l'interfaccia AutoCloseable per permettere la gestione automatica delle risorse.
 */
public class DataLayer implements AutoCloseable {

    private final DataSource datasource;
    private Connection connection;
    private final Map<Class, DAO> daos;
    private final DataCache cache;

    /**
     * Costruttore della classe DataLayer. Stabilisce una connessione con il database 
     * utilizzando il DataSource fornito.
     */
    public DataLayer(DataSource datasource) throws SQLException {
        super();
        this.datasource = datasource;
        this.connection = datasource.getConnection();
        this.daos = new HashMap<>();
        this.cache = new DataCache();
    }

    //DAO->Model
    public void registerDAO(Class entityClass, DAO dao) throws DataException {
        daos.put(entityClass, dao);
        dao.init();
    }

    //recupera il dao associato ad un model 
    public DAO getDAO(Class entityClass) {
        return daos.get(entityClass);
    }

    //inizializza i DAO 
    public void init() throws DataException {
         registerDAO(Richiesta.class, new RichiestaDAO_MySQL(this));
         registerDAO(Caratteristica.class, new CaratteristicaDAO_MySQL(this));
         //Altri DAO
        // registerDAO(RichiestaOrdine.class, new RichiestaOrdineDAO_MySQL(this));
        //call registerDAO for your own DAOs
    }

    //chiude la connessione al db e rilascia le risorse 
    public void destroy() {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException ex) {
            //
        }
    }

    public DataSource getDatasource() {
        return datasource;
    }

    public Connection getConnection() {
        return connection;
    }

    public DataCache getCache() {
        return cache;
    }

    //metodo dell'interfaccia AutoCloseable (permette di usare questa classe nei try-with-resources)
    @Override
    public void close() throws Exception {
        destroy();
    }
}
