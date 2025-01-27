package it.univaq.f4i.iw.ex.newspaper.data.model;

import it.univaq.f4i.iw.framework.data.DataItem;

/**
 *
 * @author Giuseppe Della Penna
 */
public interface Author extends DataItem<Integer> {

    String getName();

    String getSurname();

    void setName(String name);

    void setSurname(String surname);

}
