package it.univaq.f4i.iw.ex.newspaper.data.dao.impl;

import it.univaq.f4i.iw.ex.newspaper.data.dao.UserDAO;
import it.univaq.f4i.iw.ex.newspaper.data.model.User;
import it.univaq.f4i.iw.ex.newspaper.data.model.impl.proxy.UserProxy;
import it.univaq.f4i.iw.framework.data.DAO;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.data.DataItemProxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import it.univaq.f4i.iw.framework.data.DataLayer;
import it.univaq.f4i.iw.framework.data.OptimisticLockException;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Statement;

/**
 *
 * @author Giuseppe Della Penna
 */
public class UserDAO_MySQL extends DAO implements UserDAO {

    private PreparedStatement sUserByID, sUserByName, iUser, uUser;

    public UserDAO_MySQL(DataLayer d) {
        super(d);
    }

    @Override
    public void init() throws DataException {
        try {
            super.init();

            //precompiliamo tutte le query utilizzate nella classe
            //precompile all the queries uses in this class
            sUserByID = connection.prepareStatement("SELECT * FROM user WHERE ID=?");
            sUserByName = connection.prepareStatement("SELECT ID FROM user WHERE username=?");
            iUser = connection.prepareStatement("INSERT INTO user (username,password) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
            uUser = connection.prepareStatement("UPDATE user SET username=?,password=?,version=? WHERE ID=? and version=?");
        } catch (SQLException ex) {
            throw new DataException("Error initializing newspaper data layer", ex);
        }
    }

    @Override
    public void destroy() throws DataException {
        //anche chiudere i PreparedStamenent è una buona pratica...
        //also closing PreparedStamenents is a good practice...
        try {
            sUserByID.close();
            sUserByName.close();
            iUser.close();
            uUser.close();

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
    public User createUser() {
        return new UserProxy(getDataLayer());
    }

    //helper
    private UserProxy createUser(ResultSet rs) throws DataException {
        try {
            UserProxy a = (UserProxy) createUser();
            a.setKey(rs.getInt("ID"));
            a.setUsername(rs.getString("username"));
            //a.setPassword(rs.getString("password"));
            a.setPassword(SecurityHelpers.getPasswordHashPBKDF2("p")); //for testing purposes only!
            a.setVersion(rs.getLong("version"));
            return a;
        } catch (SQLException ex) {
            throw new DataException("Unable to create user object form ResultSet", ex);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            //for testing purposes only!
            throw new DataException("Password encoding problem (test)", ex);
        }
    }

    @Override
    public User getUser(int user_key) throws DataException {
        User u = null;
        //prima vediamo se l'oggetto è già stato caricato
        //first look for this object in the cache
        if (dataLayer.getCache().has(User.class, user_key)) {
            u = dataLayer.getCache().get(User.class, user_key);
        } else {
            //altrimenti lo carichiamo dal database
            //otherwise load it from database
            try {
                sUserByID.setInt(1, user_key);
                try ( ResultSet rs = sUserByID.executeQuery()) {
                    if (rs.next()) {
                        //notare come utilizziamo il costrutture
                        //"helper" della classe AuthorImpl
                        //per creare rapidamente un'istanza a
                        //partire dal record corrente
                        //note how we use here the helper constructor
                        //of the AuthorImpl class to quickly
                        //create an instance from the current record

                        u = createUser(rs);
                        //e lo mettiamo anche nella cache
                        //and put it also in the cache
                        dataLayer.getCache().add(User.class, u);
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load user by ID", ex);
            }
        }
        return u;
    }

    @Override
    public User getUserByName(String username) throws DataException {

        try {
            sUserByName.setString(1, username);
            try ( ResultSet rs = sUserByName.executeQuery()) {
                if (rs.next()) {
                    return getUser(rs.getInt("ID"));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to find user", ex);
        }
        return null;
    }

    @Override
    public void storeUser(User user) throws DataException {
        try {
            if (user.getKey() != null && user.getKey() > 0) { //update
                //non facciamo nulla se l'oggetto è un proxy e indica di non aver subito modifiche
                //do not store the object if it is a proxy and does not indicate any modification
                if (user instanceof DataItemProxy && !((DataItemProxy) user).isModified()) {
                    return;
                }
                uUser.setString(1, user.getUsername());
                uUser.setString(2, user.getPassword());

                long current_version = user.getVersion();
                long next_version = current_version + 1;

                uUser.setLong(3, next_version);
                uUser.setInt(4, user.getKey());
                uUser.setLong(5, current_version);

                if (uUser.executeUpdate() == 0) {
                    throw new OptimisticLockException(user);
                } else {
                    user.setVersion(next_version);
                }
            } else { //insert
                iUser.setString(1, user.getUsername());
                iUser.setString(2, user.getPassword());

                if (iUser.executeUpdate() == 1) {
                    //per leggere la chiave generata dal database
                    //per il record appena inserito, usiamo il metodo
                    //getGeneratedKeys sullo statement.
                    //to read the generated record key from the database
                    //we use the getGeneratedKeys method on the same statement
                    try ( ResultSet keys = iUser.getGeneratedKeys()) {
                        //il valore restituito è un ResultSet con un record
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
                            user.setKey(key);
                            //inseriamo il nuovo oggetto nella cache
                            //add the new object to the cache
                            dataLayer.getCache().add(User.class, user);
                        }
                    }
                }
            }

            if (user instanceof DataItemProxy) {
                ((DataItemProxy) user).setModified(false);
            }
        } catch (SQLException | OptimisticLockException ex) {
            throw new DataException("Unable to store user", ex);
        }
    }
}
