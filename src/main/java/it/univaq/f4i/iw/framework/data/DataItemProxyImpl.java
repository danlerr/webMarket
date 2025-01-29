package it.univaq.f4i.iw.framework.data;

/**
 *
 * @author giuse
 */
/**
 * Implementazione di un DataItem con funzionalità di tracciamento delle modifiche.
 * Questo permette di identificare se un oggetto è stato modificato e necessita di un aggiornamento nel database.
 */

public class DataItemProxyImpl extends DataItemImpl implements DataItemProxy {

    private boolean modified;

    public DataItemProxyImpl() {
        this.modified = false;
    }

    @Override
    public void setModified(boolean dirty) {
        this.modified = dirty;
    }

    @Override
    public boolean isModified() {
        return modified;
    }
}
