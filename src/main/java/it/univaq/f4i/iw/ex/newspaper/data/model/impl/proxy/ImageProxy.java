package it.univaq.f4i.iw.ex.newspaper.data.model.impl.proxy;

import it.univaq.f4i.iw.ex.newspaper.data.model.impl.ImageImpl;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.data.DataItemProxy;
import it.univaq.f4i.iw.framework.data.DataLayer;
import java.io.InputStream;

public class ImageProxy extends ImageImpl implements DataItemProxy {

    protected boolean modified;
    protected DataLayer dataLayer;

    public ImageProxy(DataLayer d) {
        super();
        //dependency injection
        this.dataLayer = d;
        this.modified = false;

    }

    @Override
    public void setKey(Integer key) {
        super.setKey(key);
        this.modified = true;
    }

    @Override
    public void setCaption(String caption) {
        super.setCaption(caption);
        this.modified = true;
    }

    @Override
    public void setImageData(InputStream is) throws DataException {
        super.setImageData(is);
        this.modified = true;
    }

    @Override
    public void setImageType(String type) {
        super.setImageType(type);
        this.modified = true;
    }

    @Override
    public void setImageSize(long size) {
        super.setImageSize(size);
        this.modified = true;
    }

    @Override
    public void setFilename(String imageFilename) {
        super.setFilename(imageFilename);
        this.modified = true;
    }

    //METODI DEL PROXY
    //PROXY-ONLY METHODS
    @Override
    public void setModified(boolean dirty) {
        this.modified = dirty;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

}
