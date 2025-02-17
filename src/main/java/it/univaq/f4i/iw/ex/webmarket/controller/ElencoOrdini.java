package it.univaq.f4i.iw.ex.webmarket.controller;

import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.ex.webmarket.data.model.Ordine;

import it.univaq.f4i.iw.ex.webmarket.data.model.StatoRichiesta;
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

public class ElencoOrdini extends BaseController {
    

    private void action_default(HttpServletRequest request, HttpServletResponse response, int user) throws IOException, ServletException, TemplateManagerException, DataException {
    
    TemplateResult res = new TemplateResult(getServletContext());
    request.setAttribute("page_title", "Elenco Ordini");

    // Recupero l'utente per determinare la sua tipologia
    Utente utente = ((ApplicationDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtente(user);
    request.setAttribute("user", utente);
    // Imposto un flag per il template (true se l'utente è ordinante)
    boolean isOrdinante = utente.getTipologiaUtente().equals(TipologiaUtente.ORDINANTE);
    request.setAttribute("isOrdinante", isOrdinante);

    if (isOrdinante) {
        // Per l'ordinante, recupero gli ordini ricevuti
        java.util.List<Ordine> ordini = ((ApplicationDataLayer) request.getAttribute("datalayer")).getOrdineDAO().getOrdiniByOrdinante(user);
        // Per ogni ordine, creo un flag che indica se mostrare il bottone "recensisci tecnico".
        // Il bottone sarà visibile solo se lo stato della richiesta associata all'ordine è RISOLTA.
         // Usiamo una mappa con chiavi STRINGA
         java.util.Map<String, Boolean> recensisciFlags = new java.util.HashMap<>();
         for (Ordine o : ordini) {
             boolean showRecensisci = o.getProposta().getRichiesta().getStato().equals(StatoRichiesta.RISOLTA);
             // Convertiamo la chiave in STRINGA
             recensisciFlags.put(String.valueOf(o.getKey()), showRecensisci);
         }
         request.setAttribute("ordini", ordini);
         request.setAttribute("recensisciFlags", recensisciFlags);
    } else {
        // Per il tecnico, recupera gli ordini gestiti dal tecnico.
        request.setAttribute("ordini", ((ApplicationDataLayer) request.getAttribute("datalayer")).getOrdineDAO().getOrdiniByTecnico(user));
    }
    
    res.activate("listaOrdini.ftl.html", request, response);
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
            Logger.getLogger(ElencoOrdini.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "Storico Ordini servlet";
    }
}