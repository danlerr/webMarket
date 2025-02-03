package it.univaq.f4i.iw.ex.webmarket.data.dao.impl;
import it.univaq.f4i.iw.ex.webmarket.controller.ElencoCategorie;
import it.univaq.f4i.iw.ex.webmarket.data.dao.CaratteristicaDAO;
import it.univaq.f4i.iw.ex.webmarket.data.dao.CategoriaDAO;
import it.univaq.f4i.iw.ex.webmarket.data.dao.UtenteDAO;
import it.univaq.f4i.iw.ex.webmarket.data.dao.OrdineDAO;
import it.univaq.f4i.iw.ex.webmarket.data.dao.PropostaDAO;
import it.univaq.f4i.iw.ex.webmarket.data.dao.RichiestaDAO;
import it.univaq.f4i.iw.ex.webmarket.data.dao.CaratteristicheRichiestaDAO;
import it.univaq.f4i.iw.ex.webmarket.data.model.Caratteristica;
import it.univaq.f4i.iw.ex.webmarket.data.model.Categoria;
import it.univaq.f4i.iw.ex.webmarket.data.model.Ordine;
import it.univaq.f4i.iw.ex.webmarket.data.model.PropostaAcquisto;
import it.univaq.f4i.iw.ex.webmarket.data.model.Richiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.Utente;
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
        registerDAO(PropostaAcquisto.class, new PropostaDAO_MySQL(this));
        registerDAO(Caratteristica.class, new CaratteristicaDAO_MySQL(this));
        registerDAO(ElencoCategorie.class, new CaratteristicheRichiestaDAO_MySQL(this));
    
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
    public PropostaDAO getPropostaAcquistoDAO() {
       return (PropostaDAO) getDAO(Proposta.class);
    }
    
     public CaratteristicaDAO getCaratteristicaDAO() {
       return (CaratteristicaDAO) getDAO(Caratteristica.class);
    }
     
     public CaratteristicheRichiestaDAO getCaratteristicaRichiestaDAO() {
       return (CaratteristicheRichiestaDAO) getDAO(CaratteristicheRichiesta.class);
    }
    
     

}
