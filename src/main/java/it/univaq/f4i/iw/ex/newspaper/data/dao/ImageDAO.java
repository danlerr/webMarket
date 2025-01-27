package it.univaq.f4i.iw.ex.newspaper.data.dao;

import it.univaq.f4i.iw.ex.newspaper.data.model.Article;
import it.univaq.f4i.iw.ex.newspaper.data.model.Image;
import it.univaq.f4i.iw.ex.newspaper.data.model.Issue;
import it.univaq.f4i.iw.framework.data.DataException;
import java.util.List;

/**
 *
 * @author Giuseppe Della Penna
 */
public interface ImageDAO {

    //metodo "factory" che permettono di creare
    //e inizializzare opportune implementazioni
    //delle interfacce del modello dati, nascondendo
    //all'utente tutti i particolari
    //factory method to create and initialize
    //suitable implementations of the data model interfaces,
    //hiding all the implementation details
    Image createImage();

    Image getImage(int image_key) throws DataException;

    List<Image> getImages(Article article) throws DataException;

    List<Image> getImages(Issue issue) throws DataException;

}
