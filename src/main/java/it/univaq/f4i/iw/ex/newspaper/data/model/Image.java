package it.univaq.f4i.iw.ex.newspaper.data.model;

import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.data.DataItem;
import java.io.InputStream;

/**
 *
 * @author Giuseppe Della Penna
 */
public interface Image extends DataItem<Integer> {

    String getCaption();

    void setCaption(String caption);

    InputStream getImageData() throws DataException;

    void setImageData(InputStream is) throws DataException;

    String getImageType();

    void setImageType(String type);

    long getImageSize();

    void setImageSize(long size);

    public String getFilename();

    public void setFilename(String imageFilename);

}
