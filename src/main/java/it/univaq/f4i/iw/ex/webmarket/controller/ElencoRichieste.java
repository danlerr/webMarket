package it.univaq.f4i.iw.ex.webmarket.controller;

import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.ex.webmarket.data.model.TipologiaUtente;
import it.univaq.f4i.iw.ex.webmarket.data.model.Utente;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.result.TemplateManagerException;
import it.univaq.f4i.iw.framework.result.TemplateResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ElencoRichieste extends BaseController {

    private void action_default(HttpServletRequest request, HttpServletResponse response, int user) throws IOException, ServletException, TemplateManagerException, DataException {

        TemplateResult res = new TemplateResult(getServletContext());

        // Recupera l'utente 
        Utente utente = ((ApplicationDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtente(user);
        request.setAttribute("user", utente);

        String template;

        if (utente.getTipologiaUtente().equals(TipologiaUtente.ORDINANTE)) {
            // Caso ordinante: si recupera l'elenco delle richieste per l'ordinante
            request.setAttribute("page_title", "Richieste Ordinante");
            request.setAttribute("richieste", ((ApplicationDataLayer) request.getAttribute("datalayer")).getRichiestaOrdineDAO().getRichiesteByUtente(user));
            template = "richiesteOrdinante.ftl.html";

        } else if (utente.getTipologiaUtente().equals(TipologiaUtente.TECNICO)) {
            // Caso tecnico: si recuperano due liste, una per le richieste in attesa e l'altra per quelle prese in carico
            request.setAttribute("page_title", "Richieste Tecnico");
            
            // Richieste in attesa per il tecnico
            request.setAttribute("richiesteInAttesa", ((ApplicationDataLayer) request.getAttribute("datalayer")).getRichiestaOrdineDAO().getRichiesteInAttesa());
            
            // Richieste prese in carico dal tecnico
            request.setAttribute("richiestePreseInCaricoSenzaProposte", ((ApplicationDataLayer) request.getAttribute("datalayer")).getRichiestaOrdineDAO().getRichiestePreseInCaricoSenzaProposteByTecnico(user));
            request.setAttribute("richiestePreseInCaricoConProposte", ((ApplicationDataLayer) request.getAttribute("datalayer")).getRichiestaOrdineDAO().getRichiestePreseInCaricoConProposteByTecnico(user));

            template = "richiesteTecnico.ftl.html";

        } else {
            
            response.sendRedirect("homeAdmin");
            return;
        }
        
        res.activate(template, request, response);
    }

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            HttpSession session = SecurityHelpers.checkSession(request);
            if (session == null) {
                response.sendRedirect("login");
                return;
            }
            int userId = (int) session.getAttribute("userid");
            
            // Azione di default: visualizza le richieste
            action_default(request, response, userId);
        } catch (IOException | TemplateManagerException ex) {
            handleError(ex, request, response);
        } catch (DataException ex) {
            Logger.getLogger(ElencoRichieste.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "Servlet per mostrare le richieste ";
    }
}

