package it.univaq.f4i.iw.ex.newspaper.data.model.impl.proxy;

import it.univaq.f4i.iw.ex.newspaper.data.model.impl.AuthorImpl;
import it.univaq.f4i.iw.framework.data.DataItemProxy;
import it.univaq.f4i.iw.framework.data.DataLayer;


public class AuthorProxy extends AuthorImpl implements DataItemProxy  {

    protected boolean modified;
    protected DataLayer dataLayer;

    public AuthorProxy(DataLayer d) {
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
    public void setName(String name) {
        super.setName(name);
        this.modified = true;
    }

    @Override
    public void setSurname(String surname) {
        super.setSurname(surname);
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
