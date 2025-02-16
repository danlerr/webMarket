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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Session;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class RecensisciTecnico extends BaseController {
    
    /**
     * Action default: visualizza la pagina per recensire il tecnico.
     * Se l'utente ha già recensito il tecnico, imposta un messaggio con il valore precedente.
     */
    private void action_default(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException, ServletException, TemplateManagerException, DataException {
        
        TemplateResult res = new TemplateResult(getServletContext());
        request.setAttribute("page_title", "Recensisci Tecnico");
        Utente utente = ((ApplicationDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtente(userId);
        request.setAttribute("user", utente);
        // Recupera l'id dell'ordine dal parametro "n"
        String ordineIdStr = request.getParameter("n");
        if (ordineIdStr == null) {
            response.sendRedirect("elencoOrdini?error=Missing+order+id");
            return;
        }
        int ordineId = Integer.parseInt(ordineIdStr);
        
        ApplicationDataLayer dal = (ApplicationDataLayer) request.getAttribute("datalayer");
        Ordine ordine = dal.getOrdineDAO().getOrdine(ordineId);
        if (ordine == null) {
            response.sendRedirect("error?Ordine+non+trovato");
            return;
        }
        
        // Verifica che l'utente loggato sia l'autore della richiesta
        Utente ordinante = ordine.getProposta().getRichiesta().getOrdinante();
        if (ordinante.getId() != userId) {
            response.sendRedirect("elencoOrdini?error=Non+sei+l'autore+della+richiesta");
            return;
        }
        
        // Recupera il tecnico associato all'ordine
        Utente tecnico = ordine.getProposta().getRichiesta().getTecnico();
        request.setAttribute("tecnico", tecnico);
        request.setAttribute("ordine", ordine);
        
        // Verifica se l'ordinante ha già recensito questo tecnico
        Recensione recensionePrecedente = dal.getRecensioneDAO().getRecensioneByOrdinanteTecnico(ordinante.getId(), tecnico.getId());
        if (recensionePrecedente != null) {
            request.setAttribute("reviewExists", true);
            request.setAttribute("oldRating", recensionePrecedente.getValore());
            request.setAttribute("message", "Hai già votato il tecnico " + tecnico.getUsername() +
                    " . Vuoi aggiornare la recensione?");
        } else {
            request.setAttribute("reviewExists", false);
            request.setAttribute("message", "Recensisci da 1 a 5 stelle la tua esperienza con il tecnico " + tecnico.getUsername() );
        }
        
        // Attiva il template freemarker
        res.activate("recensisciTecnico.ftl.html", request, response);
    }
    
    /**
     * Action inviaRecensione: gestisce l'invio del voto.
     * Se esiste già una recensione, la aggiorna; altrimenti, la crea.
     */
    private void action_inviaRecensione(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException, ServletException, TemplateManagerException, DataException {
        
        ApplicationDataLayer dal = (ApplicationDataLayer) request.getAttribute("datalayer");
        
        // Recupera l'id dell'ordine
        String ordineIdStr = request.getParameter("n");
        if (ordineIdStr == null) {
            response.sendRedirect("error?Missing+order+id");
            return;
        }
        int ordineId = Integer.parseInt(ordineIdStr);
        
        Ordine ordine = dal.getOrdineDAO().getOrdine(ordineId);
        if (ordine == null) {
            response.sendRedirect("elencoOrdini?error=Ordine+non+trovato");
            return;
        }
        
        // Verifica che lo stato della richiesta sia RISOLTA
        if (ordine.getProposta().getRichiesta().getStato() != StatoRichiesta.RISOLTA) {
            response.sendRedirect("elencoOrdini?error=Non+puoi+recensire+il+tecnico+per+questo+ordine");
            return;
        }
        
        // Verifica che l'utente sia l'autore della richiesta
        Utente ordinante = ordine.getProposta().getRichiesta().getOrdinante();
        if (ordinante.getId() != userId) {
            response.sendRedirect("elencoOrdini?error=Non+sei+l'autore+della+richiesta");
            return;
        }
        
        // Preleva il voto (rating) dal form
        String ratingStr = request.getParameter("rating");
        if (ratingStr == null || ratingStr.isEmpty()) {
            response.sendRedirect("recensisciTecnico?n=" + ordineId + "&error=Seleziona+un+voto");
            return;
        }
        int rating = Integer.parseInt(ratingStr);
        
        // Recupera il tecnico associato all'ordine
        Utente tecnico = ordine.getProposta().getRichiesta().getTecnico();
        
        // Controlla se esiste già una recensione per questo tecnico da parte dell'ordinante
        Recensione recensione = dal.getRecensioneDAO().getRecensioneByOrdinanteTecnico(ordinante.getId(), tecnico.getId());
        
        Session emailSession = EmailSender.getEmailSession();
        String subject;
        String body;
        
        if (recensione != null) {
            // Aggiorna la recensione esistente
            recensione.setValore(rating);
            dal.getRecensioneDAO().storeRecensione(recensione);
            
            subject = "Aggiornamento Recensione Ricevuta";
            body = "<h1>Nuovo Voto Aggiornato</h1>"
                    + "<p>Hai ricevuto un aggiornamento della recensione.</p>"
                    + "<p>Nuovo voto: <strong>" + rating + "</strong></p>"
                    + "<p>Recensito da: " + ordinante.getUsername() + " (" + ordinante.getEmail() + ")</p>";
            
            EmailSender.sendEmail(emailSession, tecnico.getEmail(), subject, body);
            response.sendRedirect("recensisciTecnico?n=" + ordineId + "&success=Recensione+aggiornata+con+successo");
        } else {
            // Crea una nuova recensione
            Recensione nuovaRecensione = dal.getRecensioneDAO().createRecensione();
            nuovaRecensione.setValore(rating);
            nuovaRecensione.setAutore(ordinante);
            nuovaRecensione.setDestinatario(tecnico);
            dal.getRecensioneDAO().storeRecensione(nuovaRecensione);
            
            subject = "Nuova Recensione Ricevuta";
            body = "<h1>Nuovo Voto Ricevuto</h1>"
                    + "<p>Hai ricevuto una nuova recensione.</p>"
                    + "<p>Voto: <strong>" + rating + "</strong></p>"
                    + "<p>Recensito da: " + ordinante.getUsername() + " (" + ordinante.getEmail() + ")</p>";
            
            EmailSender.sendEmail(emailSession, tecnico.getEmail(), subject, body);
            response.sendRedirect("recensisciTecnico?n=" + ordineId + "&success=Recensione+effettuata+con+successo");
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
            int userId = (int) session.getAttribute("userid");
            
            // Verifica che l'utente loggato sia di tipologia ORDINANTE
            ApplicationDataLayer dal = (ApplicationDataLayer) request.getAttribute("datalayer");
            Utente utente = dal.getUtenteDAO().getUtente(userId);
            if (!utente.getTipologiaUtente().equals(TipologiaUtente.ORDINANTE)) {
                response.sendRedirect("elencoOrdini?error=Solo+l'ordinante+puo+recensire+il+tecnico");
                return;
            }
            
            // Se il metodo è POST, esegue l'azione per inviare la recensione, altrimenti visualizza il form
            if ("POST".equalsIgnoreCase(request.getMethod())) {
                action_inviaRecensione(request, response, userId);
            } else {
                action_default(request, response, userId);
            }
        } catch (IOException | TemplateManagerException ex) {
            handleError(ex, request, response);
        } catch (DataException ex) {
            Logger.getLogger(RecensisciTecnico.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public String getServletInfo() {
        return "Servlet per recensire il tecnico";
    }
}
