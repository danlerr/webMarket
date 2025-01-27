package it.univaq.f4i.iw.ex.newspaper.data.dao.impl;

import it.univaq.f4i.iw.ex.newspaper.data.dao.ImageDAO;
import it.univaq.f4i.iw.ex.newspaper.data.model.impl.proxy.ImageProxy;
import it.univaq.f4i.iw.ex.newspaper.data.model.Article;
import it.univaq.f4i.iw.ex.newspaper.data.model.Image;
import it.univaq.f4i.iw.ex.newspaper.data.model.Issue;
import it.univaq.f4i.iw.framework.data.DAO;
import it.univaq.f4i.iw.framework.data.DataException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import it.univaq.f4i.iw.framework.data.DataLayer;

/**
 *
 * @author Giuseppe Della Penna
 */
public class ImageDAO_MySQL extends DAO implements ImageDAO {

    private PreparedStatement sImageByID;
    private PreparedStatement sImagesByIssue, sImagesByArticle;
    private PreparedStatement sImageData;

    public ImageDAO_MySQL(DataLayer d) {
        super(d);
    }

    @Override
    public void init() throws DataException {
        try {
            super.init();

            //precompiliamo tutte le query utilizzate nella classe
            //precompile all the queries uses in this class
            sImageByID = connection.prepareStatement("SELECT * FROM image WHERE ID=?");

            sImagesByIssue = connection.prepareStatement("SELECT article_image.imageID FROM article_image INNER JOIN article ON (article_image.articleID = article.ID) WHERE article.issueID=?");
            sImagesByArticle = connection.prepareStatement("SELECT imageID FROM article_image WHERE articleID=?");
            sImageData = connection.prepareStatement("SELECT data FROM image WHERE ID=?");

        } catch (SQLException ex) {
            throw new DataException("Error initializing newspaper data layer", ex);
        }
    }

    @Override
    public void destroy() throws DataException {
        //anche chiudere i PreparedStamenent � una buona pratica...
        //also closing PreparedStamenents is a good practice...
        try {
            sImageByID.close();

            sImagesByIssue.close();
            sImagesByArticle.close();
            sImageData.close();

        } catch (SQLException ex) {
            //
        }
        super.destroy();
    }

    @Override
    public Image createImage() {
        return new ImageProxy(getDataLayer());
    }

    //helper    
    private ImageProxy createImage(ResultSet rs) throws DataException {
        ImageProxy i = (ImageProxy)createImage();
        try {
            i.setKey(rs.getInt("ID"));
            i.setImageSize(rs.getLong("size"));
            i.setCaption(rs.getString("caption"));
            i.setImageType(rs.getString("type"));
            i.setFilename(rs.getString("filename"));
            i.setVersion(rs.getLong("version"));
        } catch (SQLException ex) {
            throw new DataException("Unable to create image object form ResultSet", ex);
        }
        return i;
    }

    @Override
    public Image getImage(int image_key) throws DataException {
        Image i = null;
        //prima vediamo se l'oggetto è già stato caricato
        //first look for this object in the cache
        if (dataLayer.getCache().has(Image.class, image_key)) {
            i = dataLayer.getCache().get(Image.class, image_key);
        } else {
            //altrimenti lo carichiamo dal database
            //otherwise load it from database
            try {
                sImageByID.clearParameters();
                sImageByID.setInt(1, image_key);
                try (ResultSet rs = sImageByID.executeQuery()) {
                    if (rs.next()) {
                        i = createImage(rs);
                        //e lo mettiamo anche nella cache
                        //and put it also in the cache
                        dataLayer.getCache().add(Image.class, i);

                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load image by ID", ex);
            }
        }
        return i;
    }

    @Override
    public List<Image> getImages(Article article) throws DataException {
        List<Image> result = new ArrayList();
        try {
            sImagesByArticle.setInt(1, article.getKey());
            try (ResultSet rs = sImagesByArticle.executeQuery()) {
                while (rs.next()) {
                    result.add(getImage(rs.getInt("ImageID")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load images by article", ex);
        }
        return result;
    }

    @Override
    public List<Image> getImages(Issue issue) throws DataException {
        List<Image> result = new ArrayList();
        try {
            sImagesByIssue.setInt(1, issue.getKey());
            try (ResultSet rs = sImagesByIssue.executeQuery()) {
                while (rs.next()) {
                    result.add(getImage(rs.getInt("imageID")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load images by issue", ex);
        }
        return result;
    }

//    @Override
//    public InputStream getImageData(int image_key) throws DataException {
//        try {
//            sImageData.setInt(1, image_key);
//            try (ResultSet rs = sImageData.executeQuery()) {
//                if (rs.next()) {
//                    return rs.getBinaryStream("data");
//                }
//
//            }
//        } catch (SQLException ex) {
//            throw new DataException("Unable to load image data by ID", ex);
//        }
//        return null;
//    }
}
