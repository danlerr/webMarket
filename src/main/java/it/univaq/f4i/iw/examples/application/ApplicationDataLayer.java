package it.univaq.f4i.iw.examples.application;

import it.univaq.f4i.iw.ex.newspaper.data.dao.ArticleDAO;
import it.univaq.f4i.iw.ex.newspaper.data.dao.AuthorDAO;
import it.univaq.f4i.iw.ex.newspaper.data.dao.ImageDAO;
import it.univaq.f4i.iw.ex.newspaper.data.dao.IssueDAO;
import it.univaq.f4i.iw.ex.newspaper.data.dao.UserDAO;
import it.univaq.f4i.iw.ex.newspaper.data.dao.impl.ArticleDAO_MySQL;
import it.univaq.f4i.iw.ex.newspaper.data.dao.impl.AuthorDAO_MySQL;
import it.univaq.f4i.iw.ex.newspaper.data.dao.impl.ImageDAO_MySQL;
import it.univaq.f4i.iw.ex.newspaper.data.dao.impl.IssueDAO_MySQL;
import it.univaq.f4i.iw.ex.newspaper.data.dao.impl.UserDAO_MySQL;
import it.univaq.f4i.iw.ex.newspaper.data.model.Article;
import it.univaq.f4i.iw.ex.newspaper.data.model.Author;
import it.univaq.f4i.iw.ex.newspaper.data.model.Image;
import it.univaq.f4i.iw.ex.newspaper.data.model.Issue;
import it.univaq.f4i.iw.ex.newspaper.data.model.User;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.data.DataLayer;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 *
 * @author Giuseppe Della Penna
 */
public class ApplicationDataLayer extends DataLayer {

    public ApplicationDataLayer(DataSource datasource) throws SQLException {
        super(datasource);
    }

    @Override
    public void init() throws DataException {
        //registriamo i nostri dao
        //register our daos
        registerDAO(Article.class, new ArticleDAO_MySQL(this));
        registerDAO(Author.class, new AuthorDAO_MySQL(this));
        registerDAO(Issue.class, new IssueDAO_MySQL(this));
        registerDAO(Image.class, new ImageDAO_MySQL(this));
        registerDAO(User.class, new UserDAO_MySQL(this));
    }

    //helpers    
    public ArticleDAO getArticleDAO() {
        return (ArticleDAO) getDAO(Article.class);
    }

    public AuthorDAO getAuthorDAO() {
        return (AuthorDAO) getDAO(Author.class);
    }

    public IssueDAO getIssueDAO() {
        return (IssueDAO) getDAO(Issue.class);
    }

    public ImageDAO getImageDAO() {
        return (ImageDAO) getDAO(Image.class);
    }
    
     public UserDAO getUserDAO() {
        return (UserDAO) getDAO(User.class);
    }

}
