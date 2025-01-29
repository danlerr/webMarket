package it.univaq.f4i.iw.framework.data;

import java.sql.Connection;

/**
 *
 * @author Giuseppe Della Penna
 */
/**
 * Classe base per i Data Access Object (DAO).
 * Fornisce un riferimento al livello di gestione dei dati (DataLayer) e alla connessione al database.
 * Tutti i DAO specifici devono estendere questa classe per ereditare la gestione della connessione.
 *
 * Funzionalità principali:
 * - Memorizza un riferimento alla connessione al database.
 * - Fornisce metodi per accedere al DataLayer e alla Connection.
 * - Definisce i metodi init() e destroy() per eventuali operazioni di inizializzazione e cleanup.
 *
 * Questa classe non è pensata per essere usata direttamente, ma come superclasse per DAO specifici.
 */
public class DAO {

    protected final DataLayer dataLayer; //riferimento al livello di gestione dei dati
    protected final Connection connection; //connessione al database ottenuta dal DataLayer

    //Costruttore che inizializza il DataLayer e la connessione al db
    public DAO(DataLayer d) {
        this.dataLayer = d;
        this.connection = d.getConnection();
    }

    //Metodi getDataLayer() e getConnection(): Accesso ai componenti di base per la gestione dei dati
    protected DataLayer getDataLayer() {
        return dataLayer;
    }

    protected Connection getConnection() {
        return connection;
    }

    public void init() throws DataException {

    }

    public void destroy() throws DataException {

    }
}
