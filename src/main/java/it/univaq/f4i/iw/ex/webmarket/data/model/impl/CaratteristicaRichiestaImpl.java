package it.univaq.f4i.iw.ex.webmarket.data.model.impl;

import it.univaq.f4i.iw.ex.webmarket.data.model.Caratteristica;
import it.univaq.f4i.iw.ex.webmarket.data.model.CaratteristicaRichiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.Richiesta;
import it.univaq.f4i.iw.framework.data.DataItemImpl;



public class CaratteristicaRichiestaImpl extends DataItemImpl<Integer> implements CaratteristicaRichiesta {
    private int id;
    private Richiesta richiesta;
    private Caratteristica caratteristica;
    private String valore;

    // Costruttori
    public CaratteristicaRichiestaImpl() {
        super();
        richiesta=null;
        caratteristica=null;
        valore=null;
    }

    public CaratteristicaRichiestaImpl(int id, Richiesta r, Caratteristica c, String valore) {
        this.id = id;
        this.richiesta = r;
        this.caratteristica = c;
        this.valore = valore;
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
    public Richiesta getRichiesta() {
        return richiesta;
    }

    @Override
    public void setRichiesta(Richiesta r) {
        this.richiesta = r;
    }

    @Override
    public Caratteristica getCaratteristica() {
        return caratteristica;
    }

    @Override
    public void setCaratteristica(Caratteristica c) {
        this.caratteristica = c;
    }

    @Override
    public String getValore() {
        return valore;
    }

    @Override
    public void setValore(String valore) {
        this.valore = valore;
    }
}
