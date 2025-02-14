package it.univaq.f4i.iw.ex.webmarket.data.model.impl.proxy;


import it.univaq.f4i.iw.ex.webmarket.data.model.Recensione;

import it.univaq.f4i.iw.ex.webmarket.data.model.Utente;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.RecensioneImpl;
import it.univaq.f4i.iw.framework.data.DataItemProxy;
import it.univaq.f4i.iw.framework.data.DataLayer;

/**
 * Proxy per l'oggetto {@link Recensione}.
 * Monitora le modifiche e interagisce con il DataLayer per la persistenza.
 */
public class RecensioneProxy extends RecensioneImpl implements DataItemProxy {
    
    protected boolean modified;
    protected DataLayer dataLayer;

    /**
     * Costruttore che accetta un DataLayer per la gestione della persistenza.
     *
     * @param d l'istanza di DataLayer.
     */
    public RecensioneProxy(DataLayer d) {
        super();
        this.dataLayer = d;
        this.modified = false;
    }

    /**
     * Imposta la chiave (ID) della recensione e marca l'oggetto come modificato.
     *
     * @param key l'ID da impostare.
     */
    @Override
    public void setKey(Integer key) {
        super.setKey(key);
        this.modified = true;
    }

    /**
     * Imposta il valore della recensione e marca l'oggetto come modificato.
     *
     * @param valore il valore da impostare (1-5).
     */
    @Override
    public void setValore(int valore) {
        super.setValore(valore);
        this.modified = true;
    }



    /**
     * Imposta l'autore della recensione e marca l'oggetto come modificato.
     *
     * @param autore l'autore da impostare.
     */
    @Override
    public void setAutore(Utente autore) {
        super.setAutore(autore);
        this.modified = true;
    }

    /**
     * Imposta il destinatario della recensione e marca l'oggetto come modificato.
     *
     * @param destinatario il destinatario da impostare.
     */
    @Override
    public void setDestinatario(Utente destinatario) {
        super.setDestinatario(destinatario);
        this.modified = true;
    }

    // METODI DEL PROXY
    // PROXY-ONLY METHODS

    /**
     * Imposta lo stato di modificato dell'oggetto.
     *
     * @param dirty true se l'oggetto è stato modificato, false altrimenti.
     */
    @Override
    public void setModified(boolean dirty) {
        this.modified = dirty;
    }

    /**
     * Restituisce lo stato di modifica dell'oggetto.
     *
     * @return true se l'oggetto è stato modificato, false altrimenti.
     */
    @Override
    public boolean isModified() {
        return modified;
    }

    
}
