package it.univaq.f4i.iw.ex.newspaper.data.model.impl.proxy;

import it.univaq.f4i.iw.ex.newspaper.data.model.impl.UserImpl;
import it.univaq.f4i.iw.framework.data.DataItemProxy;
import it.univaq.f4i.iw.framework.data.DataLayer;


public class UserProxy extends UserImpl implements DataItemProxy  {

    protected boolean modified;
    protected DataLayer dataLayer;

    public UserProxy(DataLayer d) {
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
    public void setUsername(String name) {
        super.setUsername(name);
        this.modified = true;
    }

    @Override
    public void setPassword(String surname) {
        super.setPassword(surname);
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
