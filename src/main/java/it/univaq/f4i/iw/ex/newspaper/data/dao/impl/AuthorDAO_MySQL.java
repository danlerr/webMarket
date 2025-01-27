package it.univaq.f4i.iw.ex.newspaper.data.dao.impl;

import it.univaq.f4i.iw.ex.newspaper.data.dao.AuthorDAO;
import it.univaq.f4i.iw.ex.newspaper.data.model.impl.proxy.AuthorProxy;
import it.univaq.f4i.iw.ex.newspaper.data.model.Author;
import it.univaq.f4i.iw.framework.data.DAO;
import it.univaq.f4i.iw.framework.data.DataException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import it.univaq.f4i.iw.framework.data.DataLayer;

/**
 *
 * @author Giuseppe Della Penna
 */
public class AuthorDAO_MySQL extends DAO implements AuthorDAO {

    private PreparedStatement sAuthors, sAuthorByID;

    public AuthorDAO_MySQL(DataLayer d) {
        super(d);
    }

    @Override
    public void init() throws DataException {
        try {
            super.init();

            //precompiliamo tutte le query utilizzate nella classe
            //precompile all the queries uses in this class
            sAuthorByID = connection.prepareStatement("SELECT * FROM author WHERE ID=?");
            sAuthors = connection.prepareStatement("SELECT ID FROM author");
        } catch (SQLException ex) {
            throw new DataException("Error initializing newspaper data layer", ex);
        }
    }

    @Override
    public void destroy() throws DataException {
        //anche chiudere i PreparedStamenent � una buona pratica...
        //also closing PreparedStamenents is a good practice...
        try {
            sAuthorByID.close();
            sAuthors.close();

        } catch (SQLException ex) {
            //
        }
        super.destroy();
    }

//metodi "factory" che permettono di creare
//e inizializzare opportune implementazioni
//delle interfacce del modello dati, nascondendo
//all'utente tutti i particolari
//factory methods to create and initialize
//suitable implementations of the data model interfaces,
//hiding all the implementation details
    @Override
    public Author createAuthor() {
        return new AuthorProxy(getDataLayer());
    }

    //helper
    private AuthorProxy createAuthor(ResultSet rs) throws DataException {
        try {
            AuthorProxy a = (AuthorProxy)createAuthor();
            a.setKey(rs.getInt("ID"));
            a.setName(rs.getString("name"));
            a.setSurname(rs.getString("surname"));
            a.setVersion(rs.getLong("version"));
            return a;
        } catch (SQLException ex) {
            throw new DataException("Unable to create author object form ResultSet", ex);
        }
    }

    @Override
    public Author getAuthor(int author_key) throws DataException {
        Author a = null;
        //prima vediamo se l'oggetto è già stato caricato
        //first look for this object in the cache
        if (dataLayer.getCache().has(Author.class, author_key)) {
            a = dataLayer.getCache().get(Author.class, author_key);
        } else {
            //altrimenti lo carichiamo dal database
            //otherwise load it from database
            try {
                sAuthorByID.setInt(1, author_key);
                try (ResultSet rs = sAuthorByID.executeQuery()) {
                    if (rs.next()) {
                        //notare come utilizziamo il costrutture
                        //"helper" della classe AuthorImpl
                        //per creare rapidamente un'istanza a
                        //partire dal record corrente
                        //note how we use here the helper constructor
                        //of the AuthorImpl class to quickly
                        //create an instance from the current record

                        a = createAuthor(rs);
                        //e lo mettiamo anche nella cache
                        //and put it also in the cache
                        dataLayer.getCache().add(Author.class, a);
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load author by ID", ex);
            }
        }
        return a;
    }

    @Override
    public List<Author> getAuthors() throws DataException {
        List<Author> result = new ArrayList();

        try (ResultSet rs = sAuthors.executeQuery()) {
            while (rs.next()) {
                result.add(getAuthor(rs.getInt("ID")));
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load authors", ex);
        }
        return result;
    }

}
