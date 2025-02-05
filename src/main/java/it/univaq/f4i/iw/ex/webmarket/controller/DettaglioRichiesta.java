package it.univaq.f4i.iw.ex.webmarket.controller;

import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.ex.webmarket.data.model.Ordine;
import it.univaq.f4i.iw.ex.webmarket.data.model.Proposta;
import it.univaq.f4i.iw.ex.webmarket.data.model.Richiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.StatoRichiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.Utente;
import it.univaq.f4i.iw.ex.webmarket.data.model.CaratteristicheRichiesta;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.result.TemplateManagerException;
import it.univaq.f4i.iw.framework.result.TemplateResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class DettaglioRichiesta extends BaseController {

    private void action_default(HttpServletRequest request, HttpServletResponse response, int user)
        throws IOException, ServletException, TemplateManagerException, DataException {
    TemplateResult res = new TemplateResult(getServletContext());
    request.setAttribute("page_title", "Dettaglio richiesta ");

    int richiestaId = Integer.parseInt(request.getParameter("n"));

    // Recupera la richiesta dal database utilizzando il DAO
    Richiesta richiesta = ((ApplicationDataLayer) request.getAttribute("datalayer"))
            .getRichiestaOrdineDAO().getRichiesta(richiestaId);
    request.setAttribute("richiesta", richiesta);

    // Recupera la lista delle caratteristiche relative alla richiesta
    List<CaratteristicheRichiesta> CaratteristicheRichiesta = ((ApplicationDataLayer) request.getAttribute("datalayer"))
            .getCaratteristicheRichiestaDAO().getCaratteristicheRichiestaByRichiesta(richiestaId);
    request.setAttribute("CaratteristicheRichiesta", CaratteristicheRichiesta);
    //recupera tutte le caratteristiche della richiesta

    // Recupera l'utente loggato
    Utente utente = ((ApplicationDataLayer) request.getAttribute("datalayer"))
            .getUtenteDAO().getUtente(user);

    // Se l'utente è un tecnico e la richiesta è nello stato PRESA_IN_CARICO,
    // imposta un attributo per far visualizzare il bottone "prendi in carico" nel template.
    if (utente.getTipologiaUtente().equals("TECNICO") && richiesta.getStato() == StatoRichiesta.PRESA_IN_CARICO) {
        request.setAttribute("showPrendiInCaricoButton", true);
    } else {
        request.setAttribute("showPrendiInCaricoButton", false);
    }

    res.activate("dettaglio_richiesta.ftl.html", request, response);
}


    /**
     * Azione per il tecnico "prendi in carico": modifica lo stato della richiesta
     * (impostandolo a IN_ATTESA) e reindirizza alla pagina delle richieste.
     * Questa azione viene eseguita solo se l'utente loggato è un tecnico.
     */
    private void action_prendiInCarico(HttpServletRequest request, HttpServletResponse response, int user)
            throws IOException, ServletException, TemplateManagerException, DataException {

        // Recupera l'utente loggato per verificare il ruolo
        Utente utente = ((ApplicationDataLayer) request.getAttribute("datalayer"))
                .getUtenteDAO().getUtente(user);

        // Controlla che l'utente sia un tecnico
        if (!utente.getTipologiaUtente().equals("TECNICO")) {
            // Se non è un tecnico, reindirizza con un messaggio di errore
            response.sendRedirect("ElencoRichieste?error=Solo+il+tecnico+puo+prendere+in+carico+le+richieste");
            return;
        }

        // Recupera l'ID della richiesta dal parametro "n"
        int richiestaId = Integer.parseInt(request.getParameter("n"));

        // Recupera la richiesta tramite il DAO
        Richiesta richiesta = ((ApplicationDataLayer) request.getAttribute("datalayer"))
                .getRichiestaOrdineDAO().getRichiesta(richiestaId);

        // Aggiorna lo stato della richiesta: in questo caso lo impostiamo a IN_ATTESA,
        // il che farà apparire la richiesta nella lista
        // delle "richieste prese in carico".
        richiesta.setStato(StatoRichiesta.IN_ATTESA);

        // Salva l'aggiornamento nel database
        ((ApplicationDataLayer) request.getAttribute("datalayer"))
                .getRichiestaOrdineDAO().storeRichiesta(richiesta);

        // Reindirizza l'utente alla pagina delle richieste
        response.sendRedirect("ElencoRichieste");
    }

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        try {
            HttpSession session = SecurityHelpers.checkSession(request);
            if (session == null) {
                response.sendRedirect("login");
                return;
            }
            // Recupera l'ID dell'utente dalla sessione
            int userId = (int) session.getAttribute("userid");

            // Se è presente un parametro "action" e il suo valore è "prendiInCarico", esegue l'azione corrispondente
            String action = request.getParameter("action");
            if (action != null && action.equals("prendiInCarico")) {
                action_prendiInCarico(request, response, userId);
                return;
            }

            // Altrimenti, mostra il dettaglio della richiesta (azione di default)
            action_default(request, response, userId);

        } catch (IOException | TemplateManagerException ex) {
            handleError(ex, request, response);
        } catch (DataException ex) {
            Logger.getLogger(DettaglioRichiesta.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "Dettaglio richiesta servlet";
    }
}
