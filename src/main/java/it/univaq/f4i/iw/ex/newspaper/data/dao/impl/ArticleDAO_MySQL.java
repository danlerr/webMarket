package it.univaq.f4i.iw.ex.newspaper.data.dao.impl;

import it.univaq.f4i.iw.ex.newspaper.data.dao.ArticleDAO;
import it.univaq.f4i.iw.ex.newspaper.data.model.impl.proxy.ArticleProxy;
import it.univaq.f4i.iw.ex.newspaper.data.model.Article;
import it.univaq.f4i.iw.ex.newspaper.data.model.Issue;
import it.univaq.f4i.iw.framework.data.DAO;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.data.DataItemProxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import it.univaq.f4i.iw.framework.data.DataLayer;
import it.univaq.f4i.iw.framework.data.OptimisticLockException;

/**
 *
 * @author Giuseppe Della Penna
 */
public class ArticleDAO_MySQL extends DAO implements ArticleDAO {

    private PreparedStatement sArticleByID;
    private PreparedStatement sArticles, sArticlesByIssue, sUnassignedArticles;
    private PreparedStatement iArticle, uArticle, dArticle;

    public ArticleDAO_MySQL(DataLayer d) {
        super(d);
    }

    @Override
    public void init() throws DataException {
        try {
            super.init();

            //precompiliamo tutte le query utilizzate nella classe
            //precompile all the queries uses in this class
            sArticleByID = connection.prepareStatement("SELECT * FROM article WHERE ID=?");
            sArticlesByIssue = connection.prepareStatement("SELECT ID AS articleID FROM article WHERE issueID=?");
            sArticles = connection.prepareStatement("SELECT ID AS articleID FROM article");
            sUnassignedArticles = connection.prepareStatement("SELECT ID AS articleID FROM article WHERE issueID IS NULL");

            //notare l'ultimo paametro extra di questa chiamata a
            //prepareStatement: lo usiamo per assicurarci che il JDBC
            //restituisca la chiave generata automaticamente per il
            //record inserito
            //note the last parameter in this call to prepareStatement:
            //it is used to ensure that the JDBC will sotre and return
            //the auto generated key for the inserted recors
            iArticle = connection.prepareStatement("INSERT INTO article (title,text,authorID,issueID,page) VALUES(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            uArticle = connection.prepareStatement("UPDATE article SET title=?,text=?,authorID=?,issueID=?, page=?, version=? WHERE ID=? and version=?");
            dArticle = connection.prepareStatement("DELETE FROM article WHERE ID=?");

        } catch (SQLException ex) {
            throw new DataException("Error initializing newspaper data layer", ex);
        }
    }

    @Override
    public void destroy() throws DataException {
        //anche chiudere i PreparedStamenent � una buona pratica...
        //also closing PreparedStamenents is a good practice...
        try {

            sArticleByID.close();

            sArticlesByIssue.close();
            sArticles.close();
            sUnassignedArticles.close();

            iArticle.close();
            uArticle.close();
            dArticle.close();

        } catch (SQLException ex) {
            //
        }
        super.destroy();
    }

    @Override
    public Article createArticle() {
        return new ArticleProxy(getDataLayer());
    }

    //helper
    private ArticleProxy createArticle(ResultSet rs) throws DataException {
        ArticleProxy a = (ArticleProxy) createArticle();
        try {
            a.setKey(rs.getInt("ID"));
            a.setAuthorKey(rs.getInt("authorID"));
            a.setText(rs.getString("text"));
            a.setTitle(rs.getString("title"));
            a.setIssueKey(rs.getInt("issueID"));
            a.setPage(rs.getInt("page"));
            a.setVersion(rs.getLong("version"));
        } catch (SQLException ex) {
            throw new DataException("Unable to create article object form ResultSet", ex);
        }
        return a;
    }

    @Override
    public Article getArticle(int article_key) throws DataException {
        Article a = null;
        //prima vediamo se l'oggetto è già stato caricato
        //first look for this object in the cache
        if (dataLayer.getCache().has(Article.class, article_key)) {
            a = dataLayer.getCache().get(Article.class, article_key);
        } else {
            //altrimenti lo carichiamo dal database
            //otherwise load it from database
            try {
                sArticleByID.setInt(1, article_key);
                try (ResultSet rs = sArticleByID.executeQuery()) {
                    if (rs.next()) {
                        a = createArticle(rs);
                        //e lo mettiamo anche nella cache
                        //and put it also in the cache
                        dataLayer.getCache().add(Article.class, a);
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load article by ID", ex);
            }
        }
        return a;
    }

    @Override
    public List<Article> getArticles(Issue issue) throws DataException {
        List<Article> result = new ArrayList();

        try {
            sArticlesByIssue.setInt(1, issue.getKey());
            try (ResultSet rs = sArticlesByIssue.executeQuery()) {
                while (rs.next()) {
                    //la query  estrae solo gli ID degli articoli selezionati
                    //poi sarà getArticle che, con le relative query, popolerà
                    //gli oggetti corrispondenti. Meno efficiente, ma così la
                    //logica di creazione degli articoli è meglio incapsulata
                    //the query extracts only the IDs of the selected articles 
                    //then getArticle, with its queries, will populate the 
                    //corresponding objects. Less efficient, but in this way
                    //article creation logic is better encapsulated
                    result.add((Article) getArticle(rs.getInt("articleID")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load articles by issue", ex);
        }
        return result;
    }

    @Override
    public List<Article> getArticles() throws DataException {
        List<Article> result = new ArrayList();

        try (ResultSet rs = sArticles.executeQuery()) {
            while (rs.next()) {
                result.add((Article) getArticle(rs.getInt("articleID")));
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load articles", ex);
        }
        return result;
    }

    @Override
    public List<Article> getUnassignedArticles() throws DataException {
        List<Article> result = new ArrayList();

        try (ResultSet rs = sUnassignedArticles.executeQuery()) {
            while (rs.next()) {
                result.add((Article) getArticle(rs.getInt("articleID")));
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load unassigned articles", ex);
        }
        return result;
    }

    @Override
    public void storeArticle(Article article) throws DataException {
        try {
            if (article.getKey() != null && article.getKey() > 0) { //update
                //non facciamo nulla se l'oggetto è un proxy e indica di non aver subito modifiche
                //do not store the object if it is a proxy and does not indicate any modification
                if (article instanceof DataItemProxy && !((DataItemProxy) article).isModified()) {
                    return;
                }
                uArticle.setString(1, article.getTitle());
                uArticle.setString(2, article.getText());
                if (article.getAuthor() != null) {
                    uArticle.setInt(3, article.getAuthor().getKey());
                } else {
                    uArticle.setNull(3, java.sql.Types.INTEGER);
                }
                if (article.getIssue() != null) {
                    uArticle.setInt(4, article.getIssue().getKey());
                    uArticle.setInt(5, article.getPage());
                } else {
                    uArticle.setNull(4, java.sql.Types.INTEGER);
                    uArticle.setNull(5, java.sql.Types.INTEGER);
                }

                long current_version = article.getVersion();
                long next_version = current_version + 1;

                uArticle.setLong(6, next_version);
                uArticle.setInt(7, article.getKey());
                uArticle.setLong(8, current_version);

                if (uArticle.executeUpdate() == 0) {
                    throw new OptimisticLockException(article);
                } else {
                    article.setVersion(next_version);
                }
            } else { //insert
                iArticle.setString(1, article.getTitle());
                iArticle.setString(2, article.getText());
                if (article.getAuthor() != null) {
                    iArticle.setInt(3, article.getAuthor().getKey());
                } else {
                    iArticle.setNull(3, java.sql.Types.INTEGER);
                }
                if (article.getIssue() != null) {
                    iArticle.setInt(4, article.getIssue().getKey());
                    iArticle.setInt(5, article.getPage());
                } else {
                    iArticle.setNull(4, java.sql.Types.INTEGER);
                    iArticle.setNull(5, java.sql.Types.INTEGER);
                }
                if (iArticle.executeUpdate() == 1) {
                    //per leggere la chiave generata dal database
                    //per il record appena inserito, usiamo il metodo
                    //getGeneratedKeys sullo statement.
                    //to read the generated record key from the database
                    //we use the getGeneratedKeys method on the same statement
                    try (ResultSet keys = iArticle.getGeneratedKeys()) {
                        //il valore restituito è un ResultSet con un record
                        //per ciascuna chiave generata (uno solo nel nostro caso)
                        //the returned value is a ResultSet with a distinct record for
                        //each generated key (only one in our case)
                        if (keys.next()) {
                            //i campi del record sono le componenti della chiave
                            //(nel nostro caso, un solo intero)
                            //the record fields are the key componenets
                            //(a single integer in our case)
                            int key = keys.getInt(1);
                            //aggiornaimo la chiave in caso di inserimento
                            //after an insert, uopdate the object key
                            article.setKey(key);
                            //inseriamo il nuovo oggetto nella cache
                            //add the new object to the cache
                            dataLayer.getCache().add(Article.class, article);
                        }
                    }
                }
            }

//            //se possibile, restituiamo l'oggetto appena inserito RICARICATO
//            //dal database tramite le API del modello. In tal
//            //modo terremo conto di ogni modifica apportata
//            //durante la fase di inserimento
//            //if possible, we return the just-inserted object RELOADED from the
//            //database through our API. In this way, the resulting
//            //object will ambed any data correction performed by
//            //the DBMS
//            if (key > 0) {
//                article.copyFrom(getArticle(key));
//            }
            //se abbiamo un proxy, resettiamo il suo attributo dirty
            //if we have a proxy, reset its dirty attribute
            if (article instanceof DataItemProxy) {
                ((DataItemProxy) article).setModified(false);
            }
        } catch (SQLException | OptimisticLockException ex) {
            throw new DataException("Unable to store article", ex);
        }
    }

    @Override
    public void publishArticle(Article article, Issue issue, int page) throws DataException {
        article.setIssue(issue);
        article.setPage(page);
        storeArticle(article);
    }
}
