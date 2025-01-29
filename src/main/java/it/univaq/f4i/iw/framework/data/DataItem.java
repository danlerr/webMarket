package it.univaq.f4i.iw.framework.data;

/**
 *
 * @author giuse
 * @param <KT> the key type
 */
/**
 * Interfaccia che rappresenta un oggetto gestibile dal livello di accesso ai dati.
 * Definisce metodi per ottenere e impostare una chiave univoca e una versione dell'oggetto. 
 */
public interface DataItem<KT> {

    KT getKey();

    long getVersion();

    void setKey(KT key);

    void setVersion(long version);

}
