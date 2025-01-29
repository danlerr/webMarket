package it.univaq.f4i.iw.framework.data;

/**
 *
 * @author giuse
 */
/**
 * Interfaccia che estende il comportamento di un DataItem con funzionalità di tracciamento delle modifiche.
 * Permette di verificare se un oggetto è stato modificato e di segnalarlo come "dirty".
 * Utile per ottimizzare l'aggiornamento dei dati nel database, evitando scritture inutili.
 */
public interface DataItemProxy {

    boolean isModified();

    void setModified(boolean dirty);

}
