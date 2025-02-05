package it.univaq.f4i.iw.ex.webmarket.data.dao.impl;
import it.univaq.f4i.iw.ex.webmarket.data.dao.CaratteristicaDAO;
import it.univaq.f4i.iw.ex.webmarket.data.dao.CategoriaDAO;
import it.univaq.f4i.iw.ex.webmarket.data.dao.UtenteDAO;
import it.univaq.f4i.iw.ex.webmarket.data.dao.OrdineDAO;
import it.univaq.f4i.iw.ex.webmarket.data.dao.PropostaDAO;
import it.univaq.f4i.iw.ex.webmarket.data.dao.RecensioneDAO;
import it.univaq.f4i.iw.ex.webmarket.data.dao.RichiestaDAO;
import it.univaq.f4i.iw.ex.webmarket.data.dao.CaratteristicaRichiestaDAO;
import it.univaq.f4i.iw.ex.webmarket.data.model.Caratteristica;
import it.univaq.f4i.iw.ex.webmarket.data.model.CaratteristicaRichiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.Categoria;
import it.univaq.f4i.iw.ex.webmarket.data.model.Ordine;
import it.univaq.f4i.iw.ex.webmarket.data.model.Proposta;
import it.univaq.f4i.iw.ex.webmarket.data.model.Richiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.Utente;
import it.univaq.f4i.iw.ex.webmarket.data.model.Recensione;
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
        registerDAO(Utente.class, new UtenteDAO_MySQL(this));
        registerDAO(Categoria.class, new CategoriaDAO_MySQL(this));
        registerDAO(Richiesta.class, new RichiestaDAO_MySQL(this));
        registerDAO(Ordine.class, new OrdineDAO_MySQL(this));
        registerDAO(Proposta.class, new PropostaDAO_MySQL(this));
        registerDAO(Caratteristica.class, new CaratteristicaDAO_MySQL(this));
        registerDAO(CaratteristicaRichiesta.class, new CaratteristicaRichiestaDAO_MySQL(this));
        registerDAO(Recensione.class, new RecensioneDAO_MySQL(this));
    
    }
    
    public UtenteDAO getUtenteDAO() {
        return (UtenteDAO) getDAO(Utente.class);
    }
     
     public CategoriaDAO getCategoriaDAO() {
        return (CategoriaDAO) getDAO(Categoria.class);
    }
     
     public RichiestaDAO getRichiestaOrdineDAO() {
        return (RichiestaDAO) getDAO(Richiesta.class);
    }

    public OrdineDAO getOrdineDAO() {
        return (OrdineDAO) getDAO(Ordine.class);
    }
    public PropostaDAO getPropostaDAO() {
       return (PropostaDAO) getDAO(Proposta.class);
    }
    
     public CaratteristicaDAO getCaratteristicaDAO() {
       return (CaratteristicaDAO) getDAO(Caratteristica.class);
    }
     
     public CaratteristicaRichiestaDAO getCaratteristicaRichiestaDAO() {
       return (CaratteristicaRichiestaDAO) getDAO(CaratteristicaRichiesta.class);
    }
    public RecensioneDAO getRecensioneDAO() {
        return (RecensioneDAO) getDAO(Recensione.class);
     }
}
