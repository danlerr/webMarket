package it.univaq.f4i.iw.ex.newspaper.data.model;

import it.univaq.f4i.iw.framework.data.DataItem;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Giuseppe Della Penna
 */
public interface Issue extends DataItem<Integer> {

    int getNumber();

    void setNumber(int number);

    LocalDate getDate();

    void setDate(LocalDate date);

    List<Article> getArticles();

    List<Image> getImages();

    void setArticles(List<Article> articles);

    void setImages(List<Image> images);
}
