package it.univaq.f4i.iw.ex.webmarket.data.model.impl;

import it.univaq.f4i.iw.ex.webmarket.data.model.Caratteristica;
import it.univaq.f4i.iw.ex.webmarket.data.model.Categoria;
import it.univaq.f4i.iw.framework.data.DataItemImpl;


public class CaratteristicaImpl extends DataItemImpl<Integer> implements Caratteristica{

    private String nome;
    private Categoria categoria;
    
    public CaratteristicaImpl() {
        super();
        nome = "";
        categoria = null;
    }

    public CaratteristicaImpl(String nome, Categoria categoria) {
        this.nome= nome;
        this.categoria= categoria;
    }

    @Override
    public int getId() {
        return this.getKey();
    }

    @Override
    public void setId(int id) {
        this.setKey(id);
    }

    @Override
    public String getNome() {
        return nome;
    }

    @Override
    public void setNome(String nome) {
        this.nome= nome;
    }

    @Override
    public Categoria getCategoria() {
        return categoria;
    }

    @Override
    public void setCategoria(Categoria categoria) {
        this.categoria= categoria;
    }
}