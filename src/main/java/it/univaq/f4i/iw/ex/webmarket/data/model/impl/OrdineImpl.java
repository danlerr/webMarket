package it.univaq.f4i.iw.ex.webmarket.data.model.impl;

import it.univaq.f4i.iw.ex.webmarket.data.model.Ordine;
import it.univaq.f4i.iw.ex.webmarket.data.model.Proposta;
import it.univaq.f4i.iw.framework.data.DataItemImpl;

import java.util.Date;

// Implementazione concreta dell'interfaccia Ordine

public class OrdineImpl extends DataItemImpl<Integer> implements Ordine {
    private int id;
    private StatoOrdine stato;
    private Proposta propostaAcquisto;
    private Date data;

    // Costruttori

    // Costruttore di default che inizializza lo stato e la proposta a null
    public OrdineImpl() {
        super();
        stato = null;
        propostaAcquisto = null;
        data = null;
    }

    // Costruttore per inizializzare l'Ordine con valori specificati
    public OrdineImpl(int id, StatoOrdine stato, Proposta propostaAcquisto, Date data) {
        this.id = id;
        this.stato = stato;
        this.propostaAcquisto = propostaAcquisto;
        this.data = data;
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
    public StatoOrdine getStato() {
        return stato;
    }

    @Override
    public void setStato(StatoOrdine stato) {
        this.stato = stato;
    }

    @Override
    public Proposta getProposta() {
        return propostaAcquisto;
    }

    @Override
    public void setProposta(Proposta p) {
        this.propostaAcquisto = p;
    }

    @Override
    public Date getData() {
        return data;
       
    }

    @Override
    public void setData(Date data) {
      this.data=data;
    }
}