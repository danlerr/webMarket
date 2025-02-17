package it.univaq.f4i.iw.ex.webmarket.data.dao.impl;

import it.univaq.f4i.iw.ex.webmarket.data.dao.UtenteDAO;
import it.univaq.f4i.iw.ex.webmarket.data.model.Utente;
import it.univaq.f4i.iw.ex.webmarket.data.model.TipologiaUtente;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.proxy.UtenteProxy;
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

public class UtenteDAO_MySQL extends DAO implements UtenteDAO {

    private PreparedStatement sUserByID, sUserByEmail, sUserByUsername, iUser, uUser, sUserByRole, sAllUser, dUser;

    /**
     * Costruttore della classe.
     * 
     * @param d il DataLayer da utilizzare
     */
    public UtenteDAO_MySQL(DataLayer d) {
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

            //precompiliamo tutte le query utilizzate nella classe
            sUserByID = connection.prepareStatement(
                "SELECT * FROM utente WHERE id = ?"
                );
            sUserByEmail = connection.prepareStatement(
                "SELECT id FROM utente WHERE email = ?"
                );
            sUserByUsername = connection.prepareStatement(
                "SELECT id FROM utente WHERE username = ?"
                );
            iUser = connection.prepareStatement(
                "INSERT INTO utente (email,password, tipologia_utente, username) VALUES(?,?,?,?)", Statement.RETURN_GENERATED_KEYS
                );
            uUser = connection.prepareStatement(
                "UPDATE utente SET email=?,password=?, tipologia_utente=?, username=?, version=? WHERE id=? AND version=?"
                );
            sUserByRole = connection.prepareStatement(
                "SELECT * FROM utente WHERE tipologia_utente=?"
                );
            sAllUser = connection.prepareStatement(
                "SELECT * FROM utente"
            );
            dUser = connection.prepareStatement(
                "DELETE FROM utente WHERE id = ?"
            );
        } catch (SQLException ex) {
            throw new DataException("Error initializing newspaper data layer", ex);
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
            sUserByID.close();
            sUserByEmail.close();
            iUser.close();
            uUser.close();
            sUserByRole.close();
            sAllUser.close();
            dUser.close();


        } catch (SQLException ex) {
            //
        }
        super.destroy();
    }

    /**
     * Crea una nuova istanza di Utente.
     * 
     * @return una nuova istanza di UtenteProxy
     */
    @Override
    public Utente createUtente() {
        return new UtenteProxy(getDataLayer());
    }

    /**
     * Crea una UtenteProxy a partire da un ResultSet.
     * 
     * @param rs il ResultSet da cui creare la UtenteProxy
     * @return una nuova istanza di UtenteProxy
     * @throws DataException se si verifica un errore durante la creazione
     */
    private UtenteProxy createUtente(ResultSet rs) throws DataException {
        try {
            UtenteProxy a = (UtenteProxy) createUtente();
            a.setKey(rs.getInt("id"));
            a.setUsername(rs.getString("username"));
            a.setEmail(rs.getString("email"));
            a.setPassword(rs.getString("password"));
            a.setTipologiaUtente(TipologiaUtente.valueOf(rs.getString("tipologia_utente")));
            a.setVersion(rs.getLong("version"));
            return a;
        } catch (SQLException ex) {
            throw new DataException("Unable to create user object form ResultSet", ex);
        }
    }

    /**
     * Recupera un utente dato il suo ID.
     * 
     * @param user_key l'ID dell'utente
     * @return l'utente corrispondente all'ID
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public Utente getUtente(int user_key) throws DataException {
        Utente u = null;
        //prima vediamo se l'oggetto è già stato caricato
        if (dataLayer.getCache().has(Utente.class, user_key)) {
            u = dataLayer.getCache().get(Utente.class, user_key);
        } else {
            //altrimenti lo carichiamo dal database
            try {
                sUserByID.setInt(1, user_key);
                try ( ResultSet rs = sUserByID.executeQuery()) {
                    if (rs.next()) {
                        u = createUtente(rs);
                        //e lo mettiamo anche nella cache
                        dataLayer.getCache().add(Utente.class, u);
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load user by ID", ex);
            }
        }
        return u;
    }

    /**
     * Recupera un utente dato il suo indirizzo email.
     * 
     * @param email l'indirizzo email dell'utente
     * @return l'utente corrispondente all'indirizzo email
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public Utente getUtenteByEmail(String email) throws DataException {

        try {
            sUserByEmail.setString(1, email);
            try ( ResultSet rs = sUserByEmail.executeQuery()) {
                if (rs.next()) {
                    return getUtente(rs.getInt("ID"));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to find user", ex);
        }
        return null;
    }
    
    /**
     * Recupera un utente dato il suo username.
     * 
     * @param username lo username dell'utente
     * @return l'utente corrispondente allo username
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public Utente getUtenteByUsername(String username) throws DataException {

        try {
            sUserByUsername.setString(1, username);
            try ( ResultSet rs = sUserByUsername.executeQuery()) {
                if (rs.next()) {
                    return getUtente(rs.getInt("ID"));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to find user", ex);
        }
        return null;
    }

    /**
     * recupera una lista di utenti dal db in base alla tipologia.
     */
    @Override
    public void storeUtente(Utente user) throws DataException {
        try {
            if (user.getKey() != null && user.getKey() > 0) { //update
                //non facciamo nulla se l'oggetto è un proxy e indica di non aver subito modifiche
                if (user instanceof UtenteProxy && !((UtenteProxy) user).isModified()) {
                    return;
                }
                uUser.setString(1, user.getEmail());
                uUser.setString(2, user.getPassword());
                uUser.setString(3, user.getTipologiaUtente().name());
                uUser.setString(4, user.getUsername());
                long oldVersion = user.getVersion();
                long versione = oldVersion + 1;
                uUser.setLong(5, versione);
                uUser.setInt(6, user.getKey());
                uUser.setLong(7, oldVersion);
                if(uUser.executeUpdate() == 0){
                    throw new OptimisticLockException(user);
                }else {
                    user.setVersion(versione);
                }

            } else { //insert
                iUser.setString(1, user.getEmail());
                iUser.setString(2, user.getPassword());
                iUser.setString(3, user.getTipologiaUtente().name());
                iUser.setString(4, user.getUsername());


                if (iUser.executeUpdate() == 1) {
                    //per leggere la chiave generata dal database per il record appena inserito, usiamo il metodo
                    //getGeneratedKeys sullo statement.
                    try ( ResultSet keys = iUser.getGeneratedKeys()) {
                        //il valore restituito è un ResultSet con un record per ciascuna chiave generata
                    
                        if (keys.next()) {
                            //i campi del record sono le componenti della chiave
                            //(nel nostro caso, un solo intero)
                            
                            int key = keys.getInt(1);
                            user.setKey(key);
                            dataLayer.getCache().add(Utente.class, user);
                        }
                    }
                }
            }

            if (user instanceof DataItemProxy) {
                ((DataItemProxy) user).setModified(false);
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to store user", ex);
        }
    }
    @Override
    public List<Utente> getAllByRole(TipologiaUtente t) throws DataException {
        List<Utente> result = new ArrayList<>();
        try {
            sUserByRole.setString(1, t.name()); 
            try (ResultSet r = sUserByRole.executeQuery()) {
                while (r.next()) {
                    result.add(getUtente(r.getInt("id")));
                }
            }
            return result;
        } catch (SQLException ex) {
            throw new DataException("Error loading all user by role", ex);
        }
    }
    @Override
    public List<Utente> getAll() throws DataException{
        List<Utente> result = new ArrayList<>();
        try {
            try (ResultSet r = sAllUser.executeQuery()) {
                while (r.next()) {
                    result.add(getUtente(r.getInt("id")));
                }
            }
            return result;
        } catch (SQLException ex) {
            throw new DataException("Error loading all user", ex);
        }
    }

    @Override
    public void deleteUtente(int utente_key) throws DataException {
        try {
            dUser.setInt(1, utente_key);
            dUser.executeUpdate();
        } catch (SQLException ex) {
            throw new DataException("Unable to delete user", ex);
        }
    }
}
