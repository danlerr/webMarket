package it.univaq.f4i.iw.ex.webmarket.data.model.impl;

import it.univaq.f4i.iw.ex.webmarket.data.model.Recensione;
import it.univaq.f4i.iw.ex.webmarket.data.model.Utente;
import it.univaq.f4i.iw.framework.data.DataItemImpl;


public class RecensioneImpl extends DataItemImpl<Integer> implements Recensione {

    private int valore;
    private Utente autore;
    private Utente destinatario;

   
    public RecensioneImpl() {
        super();
        this.valore = 1; 
        this.autore = null;
        this.destinatario = null;
    }

    
    public RecensioneImpl(int id, int valore, Utente autore, Utente destinatario) {
        super();
        this.setId(id); 
        this.setValore(valore); 
        this.setAutore(autore); 
        this.setDestinatario(destinatario); 
    }

    
    @Override
    public int getId() {
        return getKey(); 
    }

    
    @Override
    public void setId(int id) {
        setKey(id); 
    }

    
    @Override
    public int getValore() {
        return valore;
    }

    
    @Override
    public void setValore(int valore) {
        if (valore < 1 || valore > 5) {
            throw new IllegalArgumentException("Il valore della recensione deve essere tra 1 e 5.");
        }
        this.valore = valore;
    }

    
    @Override
    public Utente getAutore() {
        return autore;
    }

    
    @Override
    public void setAutore(Utente autore) {
        if (autore == null) {
            throw new IllegalArgumentException("L'autore della recensione non può essere nullo.");
        }
        this.autore = autore;
    }

    
    @Override
    public Utente getDestinatario() {
        return destinatario;
    }

    
    @Override
    public void setDestinatario(Utente destinatario) {
        if (destinatario == null) {
            throw new IllegalArgumentException("Il destinatario della recensione non può essere nullo.");
        }
        this.destinatario = destinatario;
    }

    
}
