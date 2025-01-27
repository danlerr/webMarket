package it.univaq.f4i.iw.ex.newspaper.data.model;

import it.univaq.f4i.iw.framework.data.DataItem;
import java.util.List;

/**
 *
 * @author Giuseppe Della Penna
 */
public interface Article extends DataItem<Integer> {

    Author getAuthor();

    void setAuthor(Author author);

    String getText();

    void setText(String text);

    String getTitle();

    void setTitle(String title);

    List<Image> getImages();

    void setImages(List<Image> images);

    void addImage(Image image);
    
    boolean isPublished();

    Issue getIssue();

    void setIssue(Issue issue);

    int getPage();

    void setPage(int page);

}
