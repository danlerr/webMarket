package it.univaq.f4i.iw.ex.webmarket.controller;

import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.ex.webmarket.data.model.Proposta;
import it.univaq.f4i.iw.ex.webmarket.data.model.Richiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.TipologiaUtente;
import it.univaq.f4i.iw.ex.webmarket.data.model.Utente;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.PropostaImpl;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.StatoProposta;
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


public class CreaProposta extends BaseController {

    private void action_default(HttpServletRequest request, HttpServletResponse response, int n , int userId) throws IOException, ServletException, TemplateManagerException, DataException {
        TemplateResult res = new TemplateResult(getServletContext());
        request.setAttribute("page_title", "Crea proposta");
        Utente utente = ((ApplicationDataLayer) request.getAttribute("datalayer"))
        .getUtenteDAO().getUtente(userId);
        request.setAttribute("user", utente);

        int richiesta_id = Integer.parseInt(request.getParameter("n"));
        
        //retrieve della richiesta
        Richiesta richiesta = ((ApplicationDataLayer) request.getAttribute("datalayer")).getRichiestaOrdineDAO().getRichiesta(richiesta_id);
        request.setAttribute("richiesta", richiesta);

        res.activate("creaProposta.ftl.html", request, response);
    }

    private void action_createProposta(HttpServletRequest request, HttpServletResponse response, int n, int userId) throws IOException, ServletException, TemplateManagerException, DataException {
        Richiesta richiesta = ((ApplicationDataLayer) request.getAttribute("datalayer")).getRichiestaOrdineDAO().getRichiesta(n);
        Utente utente = ((ApplicationDataLayer) request.getAttribute("datalayer"))
        .getUtenteDAO().getUtente(userId);
         if (utente.getTipologiaUtente().equals(TipologiaUtente.ORDINANTE)) {  
                response.sendRedirect("elencoRichieste?error=Non+sei+autorizzato");
                return;
            }
        request.setAttribute("user", utente);

        String produttore = request.getParameter("produttore");
        String prodotto = request.getParameter("prodotto");
        String codiceProdotto = request.getParameter("codiceProdotto");
        float prezzo;
        try {
            prezzo = Float.parseFloat(request.getParameter("prezzo"));
        } catch (NumberFormatException ex) {
            request.setAttribute("errorMessage", "Il prezzo deve essere un valore numerico valido.");
            action_default(request, response, n , userId);
            return;
        }
        String url = request.getParameter("url");
        String note;
        if (request.getParameter("note").isEmpty()) {
            note = null;
        } else {
            note = request.getParameter("note");
        }

        // Creazione una nuova proposta
        Proposta proposta = new PropostaImpl();
        proposta.setProduttore(produttore);
        proposta.setProdotto(prodotto);
        proposta.setCodiceProdotto(codiceProdotto);
        proposta.setPrezzo(prezzo);
        proposta.setUrl(url);
        proposta.setNote(note);
        proposta.setStatoProposta(StatoProposta.IN_ATTESA);
        proposta.setMotivazione(null);
        proposta.setRichiesta(richiesta);

        //update del db
        ((ApplicationDataLayer) request.getAttribute("datalayer")).getPropostaDAO().storeProposta(proposta);
    // *** Invia una email all'ordinante per notificare la creazione della proposta ***
    // Recupera l'ordinante dalla richiesta
    Utente ordinante = richiesta.getOrdinante();
    if (ordinante != null && ordinante.getEmail() != null && !ordinante.getEmail().isEmpty()) {
        Session emailSession = EmailSender.getEmailSession();


        // Prepara l'oggetto e il corpo della mail
        String subject = "Nuova Proposta per la tua Richiesta";
        String body = "<h1>Nuova Proposta</h1>"
                + "<p>Il tecnico ha creato una nuova proposta per la tua richiesta con codice <strong>"
                + richiesta.getCodiceRichiesta() + "</strong>.</p>"
                + "<p>Accedi al tuo account per visualizzare i dettagli della proposta.</p>";

        // Invia la email all'ordinante
        EmailSender.sendEmail(emailSession, ordinante.getEmail(), subject, body);
    }

        

        
        response.sendRedirect("dettaglioProposta?n=" + proposta.getKey() + "&success=Proposta+creata+con+successo");
    }

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException {
        try {
            HttpSession session = SecurityHelpers.checkSession(request);
            int n = SecurityHelpers.checkNumeric(request.getParameter("n"));
            

            if (session == null) {
                response.sendRedirect("login");
                return;
            }
            int userId = (int) session.getAttribute("userid");
            String action = request.getParameter("action");
            if (action != null && action.equals("invioProposta")) {
                action_createProposta(request, response, n , userId);
            } else {
                action_default(request, response, n, userId);
            }

        } catch (IOException | TemplateManagerException ex) {
            handleError(ex, request, response);
        } catch (DataException ex) {
            Logger.getLogger(CreaProposta.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "Servlet per la creazione di una nuova proposta d'acquisto";
    }
}
