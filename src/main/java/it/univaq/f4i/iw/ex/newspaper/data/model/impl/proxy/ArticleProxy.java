package it.univaq.f4i.iw.ex.newspaper.data.model.impl.proxy;

import it.univaq.f4i.iw.ex.newspaper.data.dao.AuthorDAO;
import it.univaq.f4i.iw.ex.newspaper.data.dao.ImageDAO;
import it.univaq.f4i.iw.ex.newspaper.data.dao.IssueDAO;
import it.univaq.f4i.iw.ex.newspaper.data.model.impl.ArticleImpl;
import it.univaq.f4i.iw.ex.newspaper.data.model.Author;
import it.univaq.f4i.iw.ex.newspaper.data.model.Image;
import it.univaq.f4i.iw.ex.newspaper.data.model.Issue;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.data.DataItemProxy;
import it.univaq.f4i.iw.framework.data.DataLayer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArticleProxy extends ArticleImpl implements DataItemProxy {

    protected boolean modified;
    protected int author_key = 0;
    protected int issue_key = 0;

    protected DataLayer dataLayer;

    public ArticleProxy(DataLayer d) {
        super();
        //dependency injection
        this.dataLayer = d;
        this.modified = false;
        this.issue_key = 0;
        this.author_key = 0;
    }

    @Override
    public void setKey(Integer key) {
        super.setKey(key);
        this.modified = true;
    }

    @Override
    public Author getAuthor() {
        //notare come l'autore in relazione venga caricato solo su richiesta
        //note how the related author is loaded only after it is requested
        if (super.getAuthor() == null && author_key > 0) {
            try {
                super.setAuthor(((AuthorDAO) dataLayer.getDAO(Author.class)).getAuthor(author_key));
            } catch (DataException ex) {
                Logger.getLogger(ArticleProxy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //attenzione: l'autore caricato viene legato all'oggetto in modo da non 
        //dover venir ricaricato alle richieste successive, tuttavia, questo
        //puo' rende i dati potenzialmente disallineati: se l'autore viene modificato
        //nel DB, qui rimarrà la sua "vecchia" versione
        //warning: the loaded author is embedded in this object so that further
        //requests do not require its reloading. However, this may cause data
        //problems since if the author is updated in the DB, here its "old"
        //version will be still attached
        return super.getAuthor();
    }

    @Override
    public void setAuthor(Author author) {
        super.setAuthor(author);
        this.author_key = author.getKey();
        this.modified = true;
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        this.modified = true;
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
        this.modified = true;
    }

    @Override
    public List<Image> getImages() {
        if (super.getImages() == null) {
            try {
                super.setImages(((ImageDAO) dataLayer.getDAO(Image.class)).getImages(this));
            } catch (DataException ex) {
                Logger.getLogger(ArticleProxy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return super.getImages();
    }

    @Override
    public void setImages(List<Image> images) {
        super.setImages(images);
        this.modified = true;
    }

    @Override
    public void addImage(Image image) {
        super.addImage(image);
        this.modified = true;
    }

    @Override
    public Issue getIssue() {
        if (super.getIssue() == null && issue_key > 0) {
            try {
                super.setIssue(((IssueDAO) dataLayer.getDAO(Issue.class)).getIssue(issue_key));
            } catch (DataException ex) {
                Logger.getLogger(ArticleProxy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return super.getIssue();
    }

    @Override
    public void setIssue(Issue issue) {
        super.setIssue(issue);
        if (issue != null) {
            this.issue_key = issue.getKey();
        } else {
            this.issue_key = 0;
        }
        this.modified = true;
    }

    @Override
    public void setPage(int page) {
        super.setPage(page);
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

    public void setAuthorKey(int author_key) {
        this.author_key = author_key;
        //resettiamo la cache dell'autore
        //reset author cache
        super.setAuthor(null);
    }

    public void setIssueKey(int issue_key) {
        this.issue_key = issue_key;
        super.setIssue(null);
    }
}
