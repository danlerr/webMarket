package it.univaq.f4i.iw.ex.newspaper.data.dao.impl;

import it.univaq.f4i.iw.ex.newspaper.data.dao.IssueDAO;
import it.univaq.f4i.iw.ex.newspaper.data.model.impl.proxy.IssueProxy;
import it.univaq.f4i.iw.ex.newspaper.data.model.Issue;
import it.univaq.f4i.iw.framework.data.DAO;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.data.DataItemProxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import it.univaq.f4i.iw.framework.data.DataLayer;
import it.univaq.f4i.iw.framework.data.OptimisticLockException;
import java.time.LocalDate;

/**
 *
 * @author Giuseppe Della Penna
 */
public class IssueDAO_MySQL extends DAO implements IssueDAO {

    private PreparedStatement sIssueByID;
    private PreparedStatement sIssues, sGetLatestIssueNumber, sGetLatestIssueKey;
    private PreparedStatement iIssue, uIssue, dIssue;

    public IssueDAO_MySQL(DataLayer d) {
        super(d);
    }

    @Override
    public void init() throws DataException {
        try {
            super.init();

            //precompiliamo tutte le query utilizzate nella classe
            //precompile all the queries uses in this class
            sIssueByID = connection.prepareStatement("SELECT * FROM issue WHERE ID=?");
            sIssues = connection.prepareStatement("SELECT ID FROM issue");
            sGetLatestIssueNumber = connection.prepareStatement("SELECT MAX(number) AS number FROM issue");
            sGetLatestIssueKey = connection.prepareStatement("SELECT ID FROM issue WHERE number = (SELECT MAX(number) FROM issue)");

            //notare l'ultimo parametro extra di questa chiamata a
            //prepareStatement: lo usiamo per assicurarci che il JDBC
            //restituisca la chiave generata automaticamente per il
            //record inserito
            //note the last parameter in this call to prepareStatement:
            //it is used to ensure that the JDBC will sotre and return
            //the auto generated key for the inserted recors
            iIssue = connection.prepareStatement("INSERT INTO issue (date,number) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
            uIssue = connection.prepareStatement("UPDATE issue SET date=?,number=?,version=? WHERE ID=? and version=?");
            dIssue = connection.prepareStatement("DELETE FROM issue WHERE ID=?");

        } catch (SQLException ex) {
            throw new DataException("Error initializing newspaper data layer", ex);
        }
    }

    @Override
    public void destroy() throws DataException {
        //anche chiudere i PreparedStamenent è una buona pratica...
        //also closing PreparedStamenents is a good practice...
        try {

            sIssueByID.close();
            sIssues.close();
            sGetLatestIssueNumber.close();
            sGetLatestIssueKey.close();

            iIssue.close();
            uIssue.close();
            dIssue.close();
        } catch (SQLException ex) {
            //
        }
        super.destroy();
    }

    @Override
    public Issue createIssue() {
        return new IssueProxy(getDataLayer());
    }

    //helper
    private IssueProxy createIssue(ResultSet rs) throws DataException {
        try {
            IssueProxy i = (IssueProxy) createIssue();
            i.setKey(rs.getInt("ID"));
            i.setNumber(rs.getInt("number"));
            //per leggere i nuovi tipi java.time su usa il generico getObject
            //to read the new java.time types we have to use the generic getObject
            i.setDate(rs.getObject("date", LocalDate.class));
            i.setVersion(rs.getLong("version"));
            return i;
        } catch (SQLException ex) {
            throw new DataException("Unable to create issue object form ResultSet", ex);
        }
    }

    @Override
    public Issue getIssue(int issue_key) throws DataException {
        Issue i = null;
        //prima vediamo se l'oggetto è già stato caricato
        //first look for this object in the cache
        if (dataLayer.getCache().has(Issue.class, issue_key)) {
            i = dataLayer.getCache().get(Issue.class, issue_key);
        } else {
            //altrimenti lo carichiamo dal database
            //otherwise load it from database
            try {
                sIssueByID.setInt(1, issue_key);
                try (ResultSet rs = sIssueByID.executeQuery()) {
                    if (rs.next()) {
                        i = createIssue(rs);
                        //e lo mettiamo anche nella cache
                        //and put it also in the cache
                        dataLayer.getCache().add(Issue.class, i);
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load issue by ID", ex);
            }
        }
        return i;
    }

    @Override
    public List<Issue> getIssues() throws DataException {
        List<Issue> result = new ArrayList();
        try (ResultSet rs = sIssues.executeQuery()) {
            while (rs.next()) {
                //la query  estrae solo gli ID degli issue selezionati
                //poi sarà getIssue che, con le relative query, popolerà
                //gli oggetti corrispondenti. Meno efficiente, ma così la
                //logica di creazione degli issue è meglio incapsulata
                //the query extracts only the IDs of the selected issues 
                //then getIssue, with its queries, will populate the 
                //corresponding objects. Less efficient, but in this way
                //issue creation logic is better encapsulated
                result.add(getIssue(rs.getInt("ID")));
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load issues", ex);
        }
        return result;
    }

    @Override
    public Issue getLatestIssue() throws DataException {
        try (
                ResultSet rs = sGetLatestIssueKey.executeQuery()) {
            if (rs.next()) {
                return getIssue(rs.getInt("ID"));
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load latest issue", ex);
        }
        return null;
    }

    @Override
    public int getLatestIssueNumber() throws DataException {
        //oppure, in maniera leggermente meno efficiente return getLatestIssue().getNumber();
        try (
                ResultSet rs = sGetLatestIssueNumber.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("number");
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to find latest issue", ex);
        }
        return 0;
    }

    @Override
    public void storeIssue(Issue issue) throws DataException {
        //nota: con questo metodo assumiamo che gli articoli legati al numero
        //se modificati siano sottoposti a store separatamente
        //note: this method assumes that the linked articles, if modified,
        //are stored separately
        try {
            if (issue.getKey() != null && issue.getKey() > 0) { //update
                //non facciamo nulla se l'oggetto è un proxy e indica di non aver subito modifiche
                //do not store the object if it is a proxy and does not indicate any modification
                if (issue instanceof DataItemProxy && !((DataItemProxy) issue).isModified()) {
                    return;
                }
                uIssue.setInt(2, issue.getNumber());
                //per scrivere i nuovi tipi java.time su usa il generico setObject
                //LocalDate viene trasformato in DATE
                //to write the new java.time types we have to use the generic getObject
                //LocalDate becomes DATE
                uIssue.setObject(1, issue.getDate());

                //controllo di versione
                long current_version = issue.getVersion();
                long next_version = current_version + 1;

                uIssue.setLong(3, next_version);
                uIssue.setInt(4, issue.getKey());
                uIssue.setLong(5, current_version);

                if (uIssue.executeUpdate() == 0) {
                    throw new OptimisticLockException(issue);
                } else {
                    issue.setVersion(next_version);
                }
            } else { //insert
                iIssue.setInt(2, issue.getNumber());
                if (issue.getDate() != null) {
                    iIssue.setObject(1, issue.getDate());
                } else {
                    iIssue.setNull(1, java.sql.Types.DATE);
                }
                if (iIssue.executeUpdate() == 1) {
                    //per leggere la chiave generata dal database
                    //per il record appena inserito, usiamo il metodo
                    //getGeneratedKeys sullo statement.
                    //to read the generated record key from the database
                    //we use the getGeneratedKeys method on the same statement
                    try (ResultSet keys = iIssue.getGeneratedKeys()) {
                        //il valore restituito � un ResultSet con un record
                        //per ciascuna chiave generata (uno solo nel nostro caso)
                        //the returned value is a ResultSet with a distinct record for
                        //each generated key (only one in our case)
                        if (keys.next()) {
                            //i campi del record sono le componenti della chiave
                            //(nel nostro caso, un solo intero)
                            //the record fields are the key componenets
                            //(a single integer in our case)
                            int key = keys.getInt(1);
                            //aggiornaimo la chiave in caso di inserimento
                            //after an insert, uopdate the object key
                            issue.setKey(key);
                            //inseriamo il nuovo oggetto nella cache
                            //add the new object to the cache
                            dataLayer.getCache().add(Issue.class, issue);
                        }
                    }
                }
            }

//            //se possibile, restituiamo l'oggetto appena inserito RICARICATO
//            //dal database tramite le API del modello. In tal
//            //modo terremo conto di ogni modifica apportata
//            //durante la fase di inserimento
//            //if possible, we return the just-inserted object RELOADED from the
//            //database through our API. In this way, the resulting
//            //object will ambed any data correction performed by
//            //the DBMS
//            if (key > 0) {
//                article.copyFrom(getArticle(key));
//            }
            //se abbiamo un proxy, resettiamo il suo attributo dirty
            //if we have a proxy, reset its dirty attribute
            if (issue instanceof DataItemProxy) {
                ((DataItemProxy) issue).setModified(false);
            }
        } catch (SQLException | OptimisticLockException ex) {
            throw new DataException("Unable to store issue", ex);
        }
    }

}
