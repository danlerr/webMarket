package it.univaq.f4i.iw.ex.webmarket.data.model;

import it.univaq.f4i.iw.ex.webmarket.data.model.impl.StatoProposta;
import it.univaq.f4i.iw.framework.data.DataItem;
import java.sql.Date;



public interface Proposta extends DataItem<Integer> {

    int getId();

    void setId(int id);

    
    String getProduttore();

    void setProduttore(String produttore);

    
    String getProdotto();

    void setProdotto(String prodotto);

    String getCodice();

    void setCodice(String codice);

    String getCodiceProdotto();

    void setCodiceProdotto(String codiceProdotto);    
    
    float getPrezzo();

    void setPrezzo(float prezzo);

    
    String getUrl();

    void setUrl(String url);

    String getNote();
    
    void setNote(String note);

    
    StatoProposta getStatoProposta();
   
    void setStatoProposta(StatoProposta stato);

    Date getData();
   
    void setData(Date data);
    
    String getMotivazione();
   
    void setMotivazione(String motivazione);

    Richiesta getRichiesta();
   
    void setRichiesta(Richiesta richiestaOrdine);
}