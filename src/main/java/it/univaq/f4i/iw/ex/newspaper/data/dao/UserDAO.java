package it.univaq.f4i.iw.ex.newspaper.data.dao;

import it.univaq.f4i.iw.ex.newspaper.data.model.User;
import it.univaq.f4i.iw.framework.data.DataException;

/**
 *
 * @author Giuseppe Della Penna
 */
public interface UserDAO {

    //metodo "factory" che permettono di creare
    //e inizializzare opportune implementazioni
    //delle interfacce del modello dati, nascondendo
    //all'utente tutti i particolari
    //factory method to create and initialize
    //suitable implementations of the data model interfaces,
    //hiding all the implementation details
    User createUser();

    User getUser(int user_key) throws DataException;
    
    User getUserByName(String username) throws DataException;

    void storeUser(User user) throws DataException;

}
