package it.univaq.f4i.iw.ex.webmarket.controller;

import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.ex.webmarket.data.model.Ordine;
import it.univaq.f4i.iw.ex.webmarket.data.model.Proposta;
import it.univaq.f4i.iw.ex.webmarket.data.model.Richiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.StatoOrdine;
import it.univaq.f4i.iw.ex.webmarket.data.model.StatoRichiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.TipologiaUtente;
import it.univaq.f4i.iw.ex.webmarket.data.model.Utente;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.OrdineImpl;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.result.TemplateManagerException;
import it.univaq.f4i.iw.framework.result.TemplateResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import java.io.IOException;
import java.sql.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Session;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class DettaglioOrdine extends BaseController {

    /**
     * Azione di default: mostra il dettaglio dell'ordine.
     * - Se l'utente è ordinante, e l'ordine è in stato IN_ATTESA e l'utente è l'autore della richiesta,
     *   il template mostrerà i bottoni "Accetta ordine" e "Rifiuta ordine".
    **/
   private void action_default(HttpServletRequest request, HttpServletResponse response, int user)
        throws IOException, ServletException, TemplateManagerException, DataException {
    TemplateResult res = new TemplateResult(getServletContext());
    request.setAttribute("page_title", "Dettaglio Ordine");

    // Recupera l'ID dell'ordine dalla request
    int ordineId = Integer.parseInt(request.getParameter("n"));
    // Recupera l'ordine dal database
    Ordine ordine = ((ApplicationDataLayer) request.getAttribute("datalayer"))
            .getOrdineDAO().getOrdine(ordineId);
    request.setAttribute("ordine", ordine);

    // Recupera l'utente loggato per controllare il ruolo
    Utente utente = ((ApplicationDataLayer) request.getAttribute("datalayer"))
            .getUtenteDAO().getUtente(user);
    request.setAttribute("user", utente);

    // Imposta il flag per il template: mostra i bottoni di accettazione/rifiuto solo per l'ordinante
    // se l'ordine è in stato IN_ATTESA e l'utente è l'autore della richiesta associata all'ordine.
    boolean showAccettaRifiutaButtons = false;
    if (utente.getTipologiaUtente().equals(TipologiaUtente.ORDINANTE) &&
        ordine.getStato().equals(StatoOrdine.IN_ATTESA) &&
        ordine.getProposta().getRichiesta().getOrdinante().getId() == user) {
        showAccettaRifiutaButtons = true;
    }
    request.setAttribute("showAccettaRifiutaButtons", showAccettaRifiutaButtons);

    res.activate("dettaglioOrdine.ftl.html", request, response);
}

    /**
     * Azione per accettare l'ordine.
     * Eseguita solo se l'utente ordinante (autorizzato) invia l'azione.
     * Aggiorna lo stato dell'ordine a ACCETTATO e imposta la richiesta a RISOLTA.
     */
    private void action_accettaOrdine(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException, TemplateManagerException, DataException {
// Recupera l'ID dell'ordine dalla request
int ordineId = SecurityHelpers.checkNumeric(request.getParameter("n"));
Ordine ordine = ((ApplicationDataLayer) request.getAttribute("datalayer"))
        .getOrdineDAO().getOrdine(ordineId);

// Recupera l'utente loggato dalla sessione per fare il controllo
int loggedUserId = (int) request.getSession(false).getAttribute("userid");

// Controlla che l'utente loggato sia l'autore della richiesta associata all'ordine
if (ordine.getProposta().getRichiesta().getOrdinante().getId() != loggedUserId) {
    // Se non corrisponde, reindirizza con un messaggio di errore
    response.sendRedirect("ordini?error=Non+sei+l'autore+della+richiesta");
    return;
}

// Se il controllo va a buon fine, aggiorna lo stato dell'ordine e della richiesta
ordine.setStato(StatoOrdine.ACCETTATO);
Richiesta richiesta = ordine.getProposta().getRichiesta();
richiesta.setStato(StatoRichiesta.RISOLTA);
((ApplicationDataLayer) request.getAttribute("datalayer")).getRichiestaOrdineDAO().storeRichiesta(richiesta);
((ApplicationDataLayer) request.getAttribute("datalayer")).getOrdineDAO().storeOrdine(ordine);

 // Invio della notifica via email al tecnico per segnalare l'accettazione dell'ordine.
    // Recupera il tecnico dalla richiesta
    Utente tecnico = ordine.getProposta().getRichiesta().getTecnico();
    if (tecnico != null && tecnico.getEmail() != null) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.outlook.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        // Configura la sessione SMTP con le credenziali corrette
        Session emailSession = Session.getInstance(props, new javax.mail.Authenticator() {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication("webmarket.univaq@outlook.com", "your_password_here");
            }
        });

        String subject = "Ordine Accettato";
        String body = "<h1>Notifica Accettazione Ordine</h1>"
                + "<p>L'ordine associato alla tua proposta per la richiesta con codice <strong>"
                + richiesta.getCodiceRichiesta() + "</strong> è stato accettato dall'ordinante.</p>"
                ;

        EmailSender.sendEmail(emailSession, tecnico.getEmail(), subject, body);
    }

response.sendRedirect("ordini?message=Ordine+accettato+con+successo");
}


 /**
 * Azione per rifiutare l'ordine.
 * L'utente (ordinante) può scegliere di rifiutare l'ordine impostando
 * lo stato a RESPINTO_NON_CONFORME oppure a RESPINTO_NON_FUNZIONANTE.
 * Inoltre, la richiesta associata viene impostata a RISOLTA.
 */
private void action_rifiutaOrdine(HttpServletRequest request, HttpServletResponse response)
throws IOException, ServletException, TemplateManagerException, DataException {
int ordineId = SecurityHelpers.checkNumeric(request.getParameter("n"));
Ordine ordine = ((ApplicationDataLayer) request.getAttribute("datalayer"))
    .getOrdineDAO().getOrdine(ordineId);

// Recupera l'utente loggato dalla sessione per fare il controllo
int loggedUserId = (int) request.getSession(false).getAttribute("userid");

// Controlla che l'utente loggato sia l'autore della richiesta associata all'ordine
if (ordine.getProposta().getRichiesta().getOrdinante().getId() != loggedUserId) {
response.sendRedirect("ordini?error=Non+sei+l'autore+della+richiesta");
return;
}

// Recupera il tipo di rifiuto dalla request.
// Il parametro "tipoRifiuto" dovrebbe contenere, ad esempio, "nonConforme" oppure "nonFunzionante".
String tipoRifiuto = request.getParameter("tipoRifiuto");
if (tipoRifiuto != null) {
if (tipoRifiuto.equals("nonConforme")) {
    ordine.setStato(StatoOrdine.RESPINTO_NON_CONFORME);
} else if (tipoRifiuto.equals("nonFunzionante")) {
    ordine.setStato(StatoOrdine.RESPINTO_NON_FUNZIONANTE);
} else {
    // Se il parametro non corrisponde ad alcun valore atteso, puoi impostare uno stato di default
    ordine.setStato(StatoOrdine.RIFIUTATO);
}
} else {
// Se il parametro non è presente, imposta uno stato di default oppure gestisci l'errore
ordine.setStato(StatoOrdine.RIFIUTATO);
}

// Aggiorna lo stato della richiesta associata
Richiesta richiesta = ordine.getProposta().getRichiesta();
richiesta.setStato(StatoRichiesta.RISOLTA);
((ApplicationDataLayer) request.getAttribute("datalayer")).getRichiestaOrdineDAO().storeRichiesta(richiesta);
((ApplicationDataLayer) request.getAttribute("datalayer")).getOrdineDAO().storeOrdine(ordine);
 // Invia la email al tecnico per notificare il rifiuto dell'ordine.
    // Recupera il tecnico dalla richiesta
    Utente tecnico = ordine.getProposta().getRichiesta().getTecnico();
    if (tecnico != null && tecnico.getEmail() != null) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.outlook.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session emailSession = Session.getInstance(props, new javax.mail.Authenticator() {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication("webmarket.univaq@outlook.com", "your_password_here");
            }
        });

        String subject = "Ordine Rifiutato";
        String body = "<h1>Notifica Rifiuto Ordine</h1>"
                + "<p>L'ordine associato alla tua proposta per la richiesta con codice <strong>"
                + richiesta.getCodiceRichiesta() + "</strong> è stato rifiutato dall'ordinante.</p>"
                ;

        EmailSender.sendEmail(emailSession, tecnico.getEmail(), subject, body);
    }
response.sendRedirect("ordini?message=Ordine+rifiutato");
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
            if ("accettaOrdine".equals(action)) {
                action_accettaOrdine(request, response);
                return;
            } else if ("rifiutaOrdine".equals(action)) {
                action_rifiutaOrdine(request, response);
                return;
            }
        }
        // Azione di default: mostra il dettaglio dell'ordine
        action_default(request, response, userId);
    } catch (IOException | TemplateManagerException ex) {
        handleError(ex, request, response);
    } catch (DataException ex) {
        Logger.getLogger(DettaglioOrdine.class.getName()).log(Level.SEVERE, null, ex);
    }
}


    @Override
    public String getServletInfo() {
        return "Dettaglio ordine servlet";
    }
}
