package it.univaq.f4i.iw.framework.data;

/**
 *
 * @author Giuseppe Della Penna
 */
/**
 * Eccezione personalizzata per la gestione degli errori nel livello di accesso ai dati.
 * Questa classe estende Exception e viene utilizzata per segnalare problemi 
 * relativi all'interazione con il database o al recupero dei dati.
 *
 * Permette di includere un messaggio di errore e, opzionalmente, una causa sottostante.
 */
public class DataException extends Exception {

    public DataException(String message) {
        super(message);
    }

    public DataException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return super.getMessage() + (getCause() != null ? " (" + getCause().getMessage() + ")" : "");
    }
}
