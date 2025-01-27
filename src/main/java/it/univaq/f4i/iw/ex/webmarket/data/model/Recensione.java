package it.univaq.f4i.iw.ex.webmarket.data.model;

import it.univaq.f4i.iw.framework.data.DataItem;

public interface Recensione extends DataItem<Integer> {
    int getId();
    void setId(int id);

    int getValore(); 
    void setValore(int valore); 
    
    Utente getAutore(); 
    void setAutore(Utente autore); 
    
    Utente getDestinatario(); 
    void setDestinatario(Utente destinatario); 
}
    

