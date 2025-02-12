package it.univaq.f4i.iw.ex.webmarket.data.model.impl;

import it.univaq.f4i.iw.ex.webmarket.data.model.Categoria;
import it.univaq.f4i.iw.ex.webmarket.data.model.Richiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.StatoRichiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.Utente;
import it.univaq.f4i.iw.framework.data.DataItemImpl;
import java.util.Date;


public class RichiestaImpl extends DataItemImpl<Integer> implements Richiesta {
    private int id;
    private String note;
    private StatoRichiesta stato;
    private Date data;
    private String codiceRichiesta;
    private Utente ordinante;
    private Utente tecnico;
    private Categoria categoria;

    
    public RichiestaImpl() {
        super();
        this.note = "";
        this.stato = StatoRichiesta.IN_ATTESA; // Stato di default
        this.data = null;
        this.codiceRichiesta = "";
        this.ordinante = null;
        this.tecnico = null;
        this.categoria = null;
    }

    
    public RichiestaImpl(int id, String note, StatoRichiesta stato, Date data, String codiceRichiesta,
                         Utente ordinante, Utente tecnico, Categoria categoria) {
        super();
        this.setId(id); 
        this.note = note;
        this.stato = stato;
        this.data = data;
        this.codiceRichiesta = codiceRichiesta;
        this.ordinante = ordinante;
        this.tecnico = tecnico;
        this.categoria = categoria;
    }

    
    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    
    @Override
    public String getNote() {
        return note;
    }

    
    @Override
    public void setNote(String note) {
        if (note == null) {
            throw new IllegalArgumentException("Le note non possono essere nulle.");
        }
        this.note = note;
    }

    
    @Override
    public StatoRichiesta getStato() {
        return stato;
    }

    
    @Override
    public void setStato(StatoRichiesta stato) {
        if (stato == null) {
            throw new IllegalArgumentException("Lo stato della richiesta non può essere nullo.");
        }
        this.stato = stato;
    }

    
    @Override
    public Date getData() {
        return data;
    }

    
    @Override
    public void setData(Date data) {
        if (data == null) {
            throw new IllegalArgumentException("La data della richiesta non può essere nulla.");
        }
        this.data = data;
    }

   
    @Override
    public String getCodiceRichiesta() {
        return codiceRichiesta;
    }

    
    @Override
    public void setCodiceRichiesta(String codiceRichiesta) {
        if (codiceRichiesta == null || codiceRichiesta.isEmpty()) {
            throw new IllegalArgumentException("Il codice della richiesta non può essere nullo o vuoto.");
        }
        this.codiceRichiesta = codiceRichiesta;
    }

    
    @Override
    public Utente getOrdinante() {
        return ordinante;
    }

    
    @Override
    public void setOrdinante(Utente ordinante) {
        if (ordinante == null) {
            throw new IllegalArgumentException("L'utente ordinante non può essere nullo.");
        }
        this.ordinante = ordinante;
    }

    
    @Override
    public Utente getTecnico() {
        return tecnico;
    }

    
    @Override
    public void setTecnico(Utente tecnico) {
        
        this.tecnico = tecnico;
    }

    
    @Override
    public Categoria getCategoria() {
        return categoria;
    }

    
    @Override
    public void setCategoria(Categoria categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("La categoria non può essere nulla.");
        }
        this.categoria = categoria;
    }

    
    
}
