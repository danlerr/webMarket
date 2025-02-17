package it.univaq.f4i.iw.ex.webmarket.controller;

import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.ex.webmarket.data.model.Proposta;
import it.univaq.f4i.iw.ex.webmarket.data.model.TipologiaUtente;
import it.univaq.f4i.iw.ex.webmarket.data.model.Utente;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.result.TemplateManagerException;
import it.univaq.f4i.iw.framework.result.TemplateResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;


public class ElencoProposte extends BaseController {

    private void action_default(HttpServletRequest request, HttpServletResponse response, int user) throws IOException, ServletException, TemplateManagerException, DataException {
        TemplateResult result = new TemplateResult(getServletContext());
        request.setAttribute("page_title", "Elenco Proposte");

        
        Utente u = ((ApplicationDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtente(user);
        // Se l'utente non esiste, reindirizza al login
        if (u == null) {
            response.sendRedirect("login");
            return;
        }
        
        request.setAttribute("user", u);


        // Controlliamo se è un Ordinante o un Tecnico
       

        // Recuperiamo le proposte in base al ruolo
        List<Proposta> proposte;
        if (u.getTipologiaUtente().equals(TipologiaUtente.ORDINANTE)) {
            proposte = ((ApplicationDataLayer) request.getAttribute("datalayer"))
                    .getPropostaDAO().getProposteByOrdinante(user);
        } else {
            proposte = ((ApplicationDataLayer) request.getAttribute("datalayer"))
                    .getPropostaDAO().getProposteByTecnico(user);
        }


        

        // Passiamo le proposte al template
        request.setAttribute("proposte", proposte);

        
        result.activate("leMieProposte.ftl.html", request, response);
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

        // Recupero l'ID dalla sessione
        int userId = (int) session.getAttribute("userid");
        
        
        action_default(request, response, userId);

    } catch (IOException | TemplateManagerException ex) {
        handleError(ex, request, response);
    }    catch (DataException ex) {
            Logger.getLogger(ElencoProposte.class.getName()).log(Level.SEVERE, null, ex);

        }
    }

    // Descrizione della servlet
    @Override
    public String getServletInfo() {
        return "Servlet per gestire l'elenco delle proposte";
    }




}
