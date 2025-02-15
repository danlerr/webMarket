package it.univaq.f4i.iw.ex.webmarket.controller;

import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;

import it.univaq.f4i.iw.ex.webmarket.data.model.Proposta;
import it.univaq.f4i.iw.ex.webmarket.data.model.Richiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.StatoRichiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.TipologiaUtente;
import it.univaq.f4i.iw.ex.webmarket.data.model.Utente;
import it.univaq.f4i.iw.ex.webmarket.data.model.CaratteristicaRichiesta;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.result.TemplateManagerException;
import it.univaq.f4i.iw.framework.result.TemplateResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Session;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class DettaglioProposta extends BaseController {

    private void action_default(HttpServletRequest request, HttpServletResponse response, int user)
    throws IOException, ServletException, TemplateManagerException, DataException {
TemplateResult res = new TemplateResult(getServletContext());
request.setAttribute("page_title", "Dettaglio proposta");

Utente utente = ((ApplicationDataLayer) request.getAttribute("datalayer"))
            .getUtenteDAO().getUtente(user);
    request.setAttribute("user", utente);
int propostaId = Integer.parseInt(request.getParameter("n"));

// Recupera la proposta dal database
Proposta proposta = ((ApplicationDataLayer) request.getAttribute("datalayer"))
        .getPropostaDAO().getProposta(propostaId);

// Se la proposta non esiste, reindirizza o mostra errore
if (proposta == null) {
    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Proposta non trovata");
    return;
}

// Se la richiesta associata è nulla, evita errori
if (proposta.getRichiesta() == null) {
    System.out.println("⚠️ Errore: La proposta non ha una richiesta associata!");
    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Richiesta associata non trovata");
    return;
}

request.setAttribute("proposta", proposta);
res.activate("dettaglioProposta.ftl.html", request, response);
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

        action_default(request, response, userId);

    } catch (IOException | TemplateManagerException ex) {
        handleError(ex, request, response);
    } catch (DataException ex) {
        Logger.getLogger(DettaglioProposta.class.getName()).log(Level.SEVERE, null, ex);
    }
}

    @Override
    public String getServletInfo() {
        return "Dettaglio proposta servlet";
    }
}

    

//approva proposta
//respingi proposta
//invia ordine
