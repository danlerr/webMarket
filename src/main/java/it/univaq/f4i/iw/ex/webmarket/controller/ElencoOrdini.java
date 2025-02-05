package it.univaq.f4i.iw.ex.webmarket.controller;

import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.ex.webmarket.data.model.Ordine;
import it.univaq.f4i.iw.ex.webmarket.data.model.Recensione;
import it.univaq.f4i.iw.ex.webmarket.data.model.StatoRichiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.TipologiaUtente;
import it.univaq.f4i.iw.ex.webmarket.data.model.Utente;
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

public class ElencoOrdini extends BaseController {
    

    private void action_default(HttpServletRequest request, HttpServletResponse response, int user)
        throws IOException, ServletException, TemplateManagerException, DataException {
    
    TemplateResult res = new TemplateResult(getServletContext());
    request.setAttribute("page_title", "Elenco Ordini");

    // Recupera l'utente per determinare la sua tipologia
    Utente utente = ((ApplicationDataLayer) request.getAttribute("datalayer"))
            .getUtenteDAO().getUtente(user);
    // Imposta un flag per il template (true se l'utente è ordinante)
    boolean isOrdinante = utente.getTipologiaUtente().equals(TipologiaUtente.ORDINANTE);
    request.setAttribute("isOrdinante", isOrdinante);

    if (isOrdinante) {
        // Per l'ordinante, recupera gli ordini ricevuti
        java.util.List<Ordine> ordini = ((ApplicationDataLayer) request.getAttribute("datalayer"))
                .getOrdineDAO().getOrdiniByOrdinante(user);
        
        // Per ogni ordine, creiamo un flag che indica se mostrare il bottone "recensisci tecnico".
        // Il bottone sarà visibile solo se lo stato della richiesta associata all'ordine è RISOLTA.
        java.util.Map<Integer, Boolean> recensisciFlags = new java.util.HashMap<>();
        for (Ordine o : ordini) {
            boolean showRecensisci = o.getProposta().getRichiesta().getStato().equals(StatoRichiesta.RISOLTA);
            // Assumiamo che o.getKey() restituisca l'ID dell'ordine.
            recensisciFlags.put(o.getKey(), showRecensisci);
        }
        request.setAttribute("ordini", ordini);
        request.setAttribute("recensisciFlags", recensisciFlags);
    } else {
        // Per il tecnico, recupera gli ordini gestiti dal tecnico.
        request.setAttribute("ordini", ((ApplicationDataLayer) request.getAttribute("datalayer"))
                .getOrdineDAO().getOrdiniByTecnico(user));
    }
    
    res.activate("ElencoOrdini.ftl.html", request, response);
}


   


    /**
     * Azione per recensire il tecnico relativo ad un ordine.
     * Questa azione viene eseguita solo se:
     *  - L'utente loggato è l'autore della richiesta (ordinante).
     *  - Lo stato della richiesta collegata all'ordine è RISOLTA.
     * Se l'utente non è ordinante (ad esempio è un tecnico) oppure la richiesta non è in stato RISOLTA,
     * l'azione non viene eseguita.
     *
     * Il parametro "n" contiene l'id dell'ordine e il form invia il parametro "value" con il voto.
     */
    private void action_recensisciTecnico(HttpServletRequest request, HttpServletResponse response, int user) 
        throws IOException, ServletException, TemplateManagerException, DataException {
        
        // Recupera l'utente loggato per verificare il ruolo
        Utente utente = ((ApplicationDataLayer) request.getAttribute("datalayer"))
                .getUtenteDAO().getUtente(user);
        if (!utente.getTipologiaUtente().equals("ORDINANTE")) {
            response.sendRedirect("ordini?error=Solo+l'ordinante+puo+recensire+il+tecnico");
            return;
        }
        
        // Recupera l'id dell'ordine
        int ordineId = Integer.parseInt(request.getParameter("n"));
        // Recupera il valore della recensione dal parametro "value"
        int value = Integer.parseInt(request.getParameter("value"));

        // Recupera l'ordine dal DAO
        Ordine ordine = ((ApplicationDataLayer) request.getAttribute("datalayer"))
                .getOrdineDAO().getOrdine(ordineId);
        
        // Verifica che la richiesta collegata all'ordine sia in stato RISOLTA
        if (ordine.getProposta().getRichiesta().getStato() != StatoRichiesta.RISOLTA) {
            response.sendRedirect("ordini?error=Non+puoi+recensire+il+tecnico+per+questo+ordine");
            return;
        }
        
        // Controllo che l'utente loggato sia l'autore della richiesta
        if (ordine.getProposta().getRichiesta().getOrdinante().getId() != user) {
            response.sendRedirect("ordini?error=Non+sei+l'autore+della+richiesta");
            return;
        }
        
        // Verifica se l'utente ha già votato questo tecnico in una richiesta precedente
        Recensione recensionePrecedente = ((ApplicationDataLayer) request.getAttribute("datalayer"))
                .getRecensioneDAO().getRecensioneByOrdinanteTecnico(
                        ordine.getProposta().getRichiesta().getOrdinante().getId(),
                        ordine.getProposta().getRichiesta().getTecnico().getId());
        
        if (recensionePrecedente != null) {
            // L'utente ha già votato: verifichiamo se ha confermato l'aggiornamento del voto
            String confirmUpdate = request.getParameter("confirmUpdate");
            if ("true".equals(confirmUpdate)) {
                // Aggiorno il voto con il nuovo valore
                recensionePrecedente.setValore(value);
                ((ApplicationDataLayer) request.getAttribute("datalayer"))
                        .getRecensioneDAO().storeRecensione(recensionePrecedente);
                response.sendRedirect("ordini?message=Recensione+aggiornata+con+successo");
                return;
            } 
        } else {
            // Nessuna recensione esistente: creo una nuova recensione
            Recensione recensione = ((ApplicationDataLayer) request.getAttribute("datalayer"))
                    .getRecensioneDAO().createRecensione();
            recensione.setValore(value);
            recensione.setAutore(((ApplicationDataLayer) request.getAttribute("datalayer"))
                    .getUtenteDAO().getUtente(user));
            recensione.setDestinatario(ordine.getProposta().getRichiesta().getTecnico());
            
            ((ApplicationDataLayer) request.getAttribute("datalayer"))
                    .getRecensioneDAO().storeRecensione(recensione);
            
            response.sendRedirect("ordini?message=Recensione+inserita+con+successo");
        }
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
            String action = request.getParameter("action");
            if (action != null) {
                if ("recensisciTecnico".equals(action)) {
                    action_recensisciTecnico(request, response, userId);
                    return;
                }
            }
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
//controllare