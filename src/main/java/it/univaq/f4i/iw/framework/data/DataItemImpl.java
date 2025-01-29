package it.univaq.f4i.iw.framework.data;

/**
 *
 * @author giuse
 * @param <KT> the key type
 */
/**
 * Implementazione base dell'interfaccia DataItem.
 * Rappresenta un oggetto che può essere memorizzato nel database e tracciato nel tempo
 * tramite una chiave univoca e un numero di versione.
 */
public class DataItemImpl<KT> implements DataItem<KT> {

    private KT key;
    private long version;

    public DataItemImpl() {
        version = 0;
    }

    @Override
    public KT getKey() {
        return key;
    }

    @Override
    public void setKey(KT key) {
        this.key = key;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void setVersion(long version) {
        this.version = version;
    }
}
