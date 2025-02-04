package it.univaq.f4i.iw.ex.webmarket.data.dao;



import it.univaq.f4i.iw.ex.webmarket.data.model.CaratteristicheRichiesta;
import it.univaq.f4i.iw.framework.data.DataException;
import java.util.List;


public interface CaratteristicheRichiestaDAO {
    CaratteristicheRichiesta createCR();

    CaratteristicheRichiesta getCR(int cr_key) throws DataException;

    void storeCR(CaratteristicheRichiesta caratteristica) throws DataException;
    
    List<CaratteristicheRichiesta> getCaratteristicheRichiestaByRichiesta(int richiesta_key) throws DataException;

}