package it.univaq.f4i.iw.framework.data;

/**
 *
 * @author giuse
 */
/**
 * Eccezione che viene sollevata quando si verifica un conflitto di versione durante 
 * l'operazione di lock ottimista, ossia quando la versione di un oggetto non corrisponde 
 * a quella attesa, indicando che l'oggetto è stato modificato da un altro processo.
 * 
 * Estende {@link DataException} e fornisce informazioni specifiche sull'oggetto coinvolto 
 * nel conflitto di versione.
 */
public class OptimisticLockException extends DataException {

    private DataItem item;

    public OptimisticLockException(DataItem item) {
        super("Version mismatch (optimistic locking) for instance " + item.getKey() + " of class " + item.getClass().getCanonicalName());
        this.item = item;
    }

    /**
     * @return the item
     */
    public DataItem getItem() {
        return item;
    }

    /**
     * @param item the item to set
     */
    public void setItem(DataItem item) {
        this.item = item;
    }

}
