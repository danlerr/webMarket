package it.univaq.f4i.iw.ex.webmarket.data.dao;



import it.univaq.f4i.iw.ex.webmarket.data.model.CaratteristicaRichiesta;
import it.univaq.f4i.iw.framework.data.DataException;
import java.util.List;


public interface CaratteristicaRichiestaDAO {
    CaratteristicaRichiesta createCR();

    CaratteristicaRichiesta getCR(int cr_key) throws DataException;

    void storeCR(CaratteristicaRichiesta caratteristica) throws DataException;
    
    List<CaratteristicaRichiesta> getCaratteristicheRichiestaByRichiesta(int richiesta_key) throws DataException;

}