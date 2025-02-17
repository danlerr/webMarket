package it.univaq.f4i.iw.ex.webmarket.controller;

import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.ex.webmarket.data.model.Ordine;
import it.univaq.f4i.iw.ex.webmarket.data.model.Proposta;
import it.univaq.f4i.iw.ex.webmarket.data.model.Richiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.StatoRichiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.TipologiaUtente;
import it.univaq.f4i.iw.ex.webmarket.data.model.Utente;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.StatoOrdine;
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
import java.sql.Timestamp;
import java.util.Date;


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

        // Se la proposta non esiste
        if (proposta == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Proposta non trovata");
            return;
        }

        
        if (proposta.getRichiesta() == null) {
            
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Richiesta associata non trovata");
            return;
        }

        // Recupera lo stato della proposta e lo passa al template
                StatoProposta statoProposta = proposta.getStatoProposta();
                request.setAttribute("stato", statoProposta.toString()); // Converte l'enum in stringa

        request.setAttribute("proposta", proposta);
        res.activate("dettaglioProposta.ftl.html", request, response);
        }

    private void action_accettaProposta(HttpServletRequest request, HttpServletResponse response, int n) throws IOException, ServletException, TemplateManagerException,DataException {
        
        HttpSession session = SecurityHelpers.checkSession(request);
        int userId = (int) session.getAttribute("userid");
        // Recupera l'utente loggato per verificare il ruolo
        Utente utente = ((ApplicationDataLayer) request.getAttribute("datalayer"))
                .getUtenteDAO().getUtente(userId);

        
        if (!utente.getTipologiaUtente().equals(TipologiaUtente.ORDINANTE)) {
            
            response.sendRedirect("elencoProposte?error=Non+puoi+accettare+la+proposta");
            return;
        }

        
        Proposta proposta = ((ApplicationDataLayer) request.getAttribute("datalayer")).getPropostaDAO().getProposta(n);
        proposta.setStatoProposta(StatoProposta.ACCETTATO);
        ((ApplicationDataLayer) request.getAttribute("datalayer")).getPropostaDAO().storeProposta(proposta);
        String email = proposta.getRichiesta().getTecnico().getEmail();
        String username = proposta.getRichiesta().getTecnico().getUsername();
        try {
            Session emailSession = EmailSender.getEmailSession();

            String subject = "Proposta accettata";
            String body = "Ciao "+username+",\n La informiamo che la sua proposta numero " + proposta.getCodice() +"è stata accettata.";
            
            EmailSender.sendEmail(emailSession, email, subject, body);
            response.sendRedirect("dettaglioProposta?success=Proposta+accettata&n=" + proposta.getKey() );
        } catch (Exception e) {
            response.sendRedirect("dettaglioProposta?success=Proposta+accettata+,+ma+si+è+verificato+un+problema+con+la+email&n=" + proposta.getKey() );
                e.printStackTrace();
            }
            response.sendRedirect("dettaglioProposta?success=Proposta+accettata&n=" + proposta.getKey() );
    }

    private void action_rifiutaProposta(HttpServletRequest request, HttpServletResponse response, int n) throws IOException, ServletException, TemplateManagerException, DataException {
        
        HttpSession session = SecurityHelpers.checkSession(request);
        int userId = (int) session.getAttribute("userid");
        // Recupera l'utente loggato per verificare il ruolo
        Utente utente = ((ApplicationDataLayer) request.getAttribute("datalayer"))
                .getUtenteDAO().getUtente(userId);
                request.setAttribute("user", utente);

       
        if (!utente.getTipologiaUtente().equals(TipologiaUtente.ORDINANTE)) {
            
            response.sendRedirect("elencoProposte?error=Non+sei+autorizzato+a+rifiutare+la+proposta");
            return;
        }

        String motivazione = request.getParameter("motivazione");
        if (motivazione == null || motivazione.trim().isEmpty()) {
            Proposta proposta = ((ApplicationDataLayer) request.getAttribute("datalayer")).getPropostaDAO().getProposta(n);
            request.setAttribute("proposta", proposta);
            response.sendRedirect("dettaglioProposta?error=Devi+indicare+la+motivazione+del+rifiuto&n=" + proposta.getKey() );
            
            return; 
        }

        Proposta proposta = ((ApplicationDataLayer) request.getAttribute("datalayer")).getPropostaDAO().getProposta(n);
        proposta.setStatoProposta(StatoProposta.RIFIUTATO);
        
        proposta.setMotivazione(motivazione);
        
        ((ApplicationDataLayer) request.getAttribute("datalayer")).getPropostaDAO().storeProposta(proposta);
        
        
        
        
        String email = proposta.getRichiesta().getTecnico().getEmail();
        String username = proposta.getRichiesta().getTecnico().getUsername();

        try {
            Session emailSession = EmailSender.getEmailSession();

            String subject = "Proposta rifiutata";
            String body = "Ciao "+username+",\n La informiamo che la sua proposta numero " + proposta.getCodice() +"è stata rifiutata.";
            
            EmailSender.sendEmail(emailSession, email, subject, body);
            response.sendRedirect("dettaglioProposta?success=Proposta+rifiutata&n=" + proposta.getKey() );
        } catch (Exception e) {
            response.sendRedirect("dettaglioProposta?success=Proposta+rifiutata+ma+non+siamo+riusciti+ad+inviare+la+mail&n=" + proposta.getKey() );
                e.printStackTrace();
            }

            response.sendRedirect("dettaglioProposta?success=Proposta+rifiutata&n=" + proposta.getKey() );
    }
     
    private void action_inviaOrdine(HttpServletRequest request, HttpServletResponse response, int n) throws IOException, ServletException, TemplateManagerException, DataException {
        
        HttpSession session = SecurityHelpers.checkSession(request);
        int userId = (int) session.getAttribute("userid");
        // Recupera l'utente loggato per verificare il ruolo
        Utente utente = ((ApplicationDataLayer) request.getAttribute("datalayer"))
                .getUtenteDAO().getUtente(userId);

        // Controlla che l'utente sia un tecnico
        if (!utente.getTipologiaUtente().equals(TipologiaUtente.TECNICO)) {
            // Se non è un tecnico, reindirizza con un messaggio di errore
            response.sendRedirect("elencoProposte?error=Solo+il+tecnico+puo+creare+un+ordine");
            return;
        }

        Proposta proposta = ((ApplicationDataLayer) request.getAttribute("datalayer")).getPropostaDAO().getProposta(n);
        // Verifica che lo stato della proposta sia ACCETTATO
        if (!proposta.getStatoProposta().equals(StatoProposta.ACCETTATO)) {
        response.sendRedirect("dettaglioProposta?error=Proposta+non+accettata&n=" + proposta.getKey());
        return;
        }
        //cambiamo lo  stato della proposta
        proposta.setStatoProposta(StatoProposta.ORDINATO);
        ((ApplicationDataLayer) request.getAttribute("datalayer")).getPropostaDAO().storeProposta(proposta);

        //cambiamo lo stato stato della richiesta
        Richiesta richiesta = proposta.getRichiesta();
        richiesta.setStato(StatoRichiesta.ORDINATA);
        ((ApplicationDataLayer) request.getAttribute("datalayer")).getRichiestaOrdineDAO().storeRichiesta(richiesta);

        String email = richiesta.getOrdinante().getEmail();
        Ordine ordine = ((ApplicationDataLayer) request.getAttribute("datalayer"))
        .getOrdineDAO().createOrdine();

        ordine.setProposta(proposta);
        ordine.setStato(StatoOrdine.IN_ATTESA);
        ordine.setData(new Timestamp(new Date().getTime()));
        ((ApplicationDataLayer) request.getAttribute("datalayer")).getOrdineDAO().storeOrdine(ordine);

        Session emailSession = EmailSender.getEmailSession();

        String subject = "Nuovo ordine effettuato!";
        String body = "<h1>Nuovo ordine</h1>"
                    + "<p>È stato creato un nuovo ordine riferito alla proposta <strong>" + proposta.getCodice() + "</strong>.</p>"
                    + "<p>Controlla la sezione degli ordini!.</p>";

        EmailSender.sendEmail(emailSession, email, subject, body);
        response.sendRedirect("dettaglioOrdine?success=Ordine+creato+con+successo&n=" + ordine.getKey()); 
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

            int n = Integer.parseInt(request.getParameter("n"));

            String action = request.getParameter("action");

            if (action != null && action.equals("accettaProposta")) {
                action_accettaProposta(request, response, n);

            }else if(action != null && action.equals("rifiutaProposta")) {
                action_rifiutaProposta(request, response, n);

            }else if(action != null && action.equals("inviaOrdine")){
                action_inviaOrdine(request, response, n);

            }else{
                action_default(request, response, userId);
            }
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
