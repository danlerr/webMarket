package it.univaq.f4i.iw.ex.webmarket.controller;

import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.ex.webmarket.data.model.Ordine;
import it.univaq.f4i.iw.ex.webmarket.data.model.Recensione;
import it.univaq.f4i.iw.ex.webmarket.data.model.StatoRichiesta;
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

public class elencoOrdini extends BaseController {

    private void action_default(HttpServletRequest request, HttpServletResponse response, int user) 
            throws IOException, ServletException, TemplateManagerException, DataException {
      
        TemplateResult res = new TemplateResult(getServletContext());
        request.setAttribute("page_title", "Ordini");

        // Imposto nella request la lista degli ordini e delle proposte dell'utente
        request.setAttribute("ordini", ((ApplicationDataLayer) request.getAttribute("datalayer"))
                .getOrdineDAO().getOrdiniByUtente(user));
        request.setAttribute("proposte", ((ApplicationDataLayer) request.getAttribute("datalayer"))
                .getPropostaDAO().getProposteByUtente(user)); 
        res.activate("ordini.ftl.html", request, response);
    }
    
    /**
     * Azione per recensire il tecnico relativo ad un ordine.
     * Questa azione è eseguita solo se la richiesta associata all'ordine ha stato "RISOLTA".
     * 
     * 
     * - Il parametro "n" contiene l'id dell'ordine.
     * - Il form di recensione invia anche il parametro "value" con il valore della recensione.
     */
    private void action_recensisciTecnico(HttpServletRequest request, HttpServletResponse response, int user) 
        throws IOException, ServletException, TemplateManagerException, DataException {
    // Recupero l'id dell'ordine
    int ordineId = Integer.parseInt(request.getParameter("n"));
    // Recupero il valore della recensione dal parametro "value"
    int value = Integer.parseInt(request.getParameter("value"));

    // Recupero l'ordine dal DAO
    Ordine ordine = ((ApplicationDataLayer) request.getAttribute("datalayer"))
            .getOrdineDAO().getOrdine(ordineId);
    
    // Verifico che la richiesta collegata all'ordine sia in stato "RISOLTA"
    if (ordine.getProposta().getRichiestaOrdine().getStato() != StatoRichiesta.RISOLTA) {
        response.sendRedirect("ordini?error=Non+puoi+recensire+il+tecnico+per+questo+ordine");
        return;
    }
    
    // Controllo che l'utente loggato sia l'autore della richiesta
    if (ordine.getProposta().getRichiestaOrdine().getAutore().getId() != user) {
        response.sendRedirect("ordini?error=Non+sei+l'autore+della+richiesta");
        return;
    }
    
    // Verifico se l'utente ha già votato questo tecnico in una richiesta precedente
    Recensione recensionePrecedente = ((ApplicationDataLayer) request.getAttribute("datalayer"))
            .getRecensioneDAO().getRecensioneByAutoreDestinatario(
                    ordine.getProposta().getRichiestaOrdine().getAutore(),
                    ordine.getProposta().getRichiestaOrdine().getTecnico());
    
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
        //******QUESTA PARTE FORSE NON SERVE*******/
        } else {
            // Non è stata confermata l'aggiornamento: 
            // Imposto nella request la recensione esistente e il nuovo valore proposto,
            // per mostrare un messaggio di conferma all'utente.
            request.setAttribute("existingRecensione", recensionePrecedente);
            request.setAttribute("newValue", value);
            // Invia all'utente una view (ad es. un template FreeMarker) che mostra:
            // "Hai già votato questo tecnico con il voto [valore_precedente]. Vuoi aggiornare il voto a [nuovo_valore]?"
            // La view dovrà mostrare un form che, al submit, invia il parametro "confirmUpdate" impostato a "true".
            TemplateResult res = new TemplateResult(getServletContext());
            res.activate("conferma_aggiornamento_recensione.ftl.html", request, response);
            return;
        }
        //******QUESTA PARTE FORSE NON SERVE*******/
    } else {
        // Nessuna recensione esistente: creo una nuova recensione
        Recensione recensione = ((ApplicationDataLayer) request.getAttribute("datalayer"))
                .getRecensioneDAO().createRecensione();
        recensione.setValore(value);
        recensione.setAutore(((ApplicationDataLayer) request.getAttribute("datalayer"))
                .getUtenteDAO().getUtente(user));
        recensione.setDestinatario(ordine.getProposta().getRichiestaOrdine().getTecnico());
        
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
            
            // Recupero l'ID dell'utente dalla sessione
            int userId = (int) session.getAttribute("userid");
            // Controllo il parametro "action" per determinare quale azione eseguire
            String action = request.getParameter("action");
            if (action != null) {
                if ("recensisciTecnico".equals(action)) {
                    action_recensisciTecnico(request, response, userId);
                    return;
                }
            }
            
            // Se nessuna azione specifica è richiesta, carico la pagina predefinita
            action_default(request, response, userId);

        } catch (IOException | TemplateManagerException ex) {
            handleError(ex, request, response);
        } catch (DataException ex) {
            Logger.getLogger(elencoOrdini.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "Storico Ordini servlet";
    }
}
