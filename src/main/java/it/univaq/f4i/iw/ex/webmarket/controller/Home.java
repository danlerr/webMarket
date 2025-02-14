package it.univaq.f4i.iw.ex.webmarket.controller;

import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.ex.webmarket.data.model.TipologiaUtente;
import it.univaq.f4i.iw.ex.webmarket.data.model.Utente;
import it.univaq.f4i.iw.framework.result.TemplateManagerException;
import it.univaq.f4i.iw.framework.result.TemplateResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class Home extends BaseController {

    private void action_default(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException, ServletException, DataException, TemplateManagerException {
        TemplateResult r = new TemplateResult(getServletContext());
        Utente u = ((ApplicationDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtente(userId);
        request.setAttribute("user", u);
        request.setAttribute("page_title", "Dashboard");
        
        Map<String, String> medieTecnici = new HashMap<>();
        Map<Integer, Double> medieTecniciOriginali = ((ApplicationDataLayer) request.getAttribute("datalayer")).getRecensioneDAO().getMedieRecensioniTecnici();
        request.setAttribute("medieTecnici", medieTecnici);
        // Converto la mappa da Integer -> Double a String -> String
        for (Map.Entry<Integer, Double> entry : medieTecniciOriginali.entrySet()) {
            String key = String.valueOf(entry.getKey());  // chiave Integer -> String
            String value = String.format("%.1f", entry.getValue());  //  Double -> String con una sola cifra decimale
            medieTecnici.put(key, value);
        }

request.setAttribute("medieTecnici", medieTecnici);  // Passiamo la nuova mappa

        List<Utente> tecnici = ((ApplicationDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getAllByRole(TipologiaUtente.TECNICO);
        request.setAttribute("tecnici", tecnici); //da tecnici prendo username e mail 

        //Map<Integer, Integer> proposteTecnici = new HashMap<>();
        Map<String, String> interventiTecnici = new HashMap<>();
        ApplicationDataLayer datalayer = (ApplicationDataLayer) request.getAttribute("datalayer");

        for (Utente tecnico : tecnici) {
            String numeroProposte = String.valueOf(datalayer.getPropostaDAO().getProposteByTecnico(tecnico.getId()).size());
            interventiTecnici.put(String.valueOf(tecnico.getId()), numeroProposte); 
        }
        request.setAttribute("interventiTecnici", interventiTecnici);

        //notifiche per richieste (solo per il tecnico | quando ci sono nuove richieste)
        boolean richieste = !((ApplicationDataLayer) request.getAttribute("datalayer")).getRichiestaOrdineDAO().getRichiesteInAttesa().isEmpty();
        request.setAttribute("richieste", richieste);
        //noticìfiche per le proposte per l'ord. 
        boolean proposte_O = ((ApplicationDataLayer) request.getAttribute("datalayer")).getPropostaDAO().notificaP_O(userId);
        request.setAttribute("proposteOrd", proposte_O);
        //notifiche per le proposte per il tecnico 
        boolean proposte_T = ((ApplicationDataLayer) request.getAttribute("datalayer")).getPropostaDAO().notificaP_T(userId);
        request.setAttribute("proposteTec", proposte_T);
        //notifiche per gli ordini per l'ordinante
        boolean ordini_O = ((ApplicationDataLayer) request.getAttribute("datalayer")).getOrdineDAO().notificaO_O(userId);
        request.setAttribute("ordiniOrd", ordini_O);
        //notifiche per gli ordini per il tecnico 
        boolean ordini_T = ((ApplicationDataLayer) request.getAttribute("datalayer")).getOrdineDAO().notificaO_T(userId);
        request.setAttribute("ordiniTec", ordini_T);


        r.activate("home.ftl.html", request, response);
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
         // Recupero l'id dell'utente dalla sessione
         int userId = (int) session.getAttribute("userid");
         Utente u = ((ApplicationDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtente(userId);
        
         if (u != null) {
            request.setAttribute("user", u);
            // Verifico se l'utente è un amministratore
            if (u.getTipologiaUtente().equals(TipologiaUtente.AMMINISTRATORE)) {  
                response.sendRedirect("homeAdmin");
                return;
            }
        }
         action_default(request, response, userId);
         }
        catch (IOException | TemplateManagerException | DataException ex) {
            handleError(ex, request, response);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Servlet home";
    }

}