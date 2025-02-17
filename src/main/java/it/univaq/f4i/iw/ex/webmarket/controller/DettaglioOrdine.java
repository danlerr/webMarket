package it.univaq.f4i.iw.ex.webmarket.controller;

import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.ex.webmarket.data.model.*;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.StatoOrdine;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.result.TemplateManagerException;
import it.univaq.f4i.iw.framework.result.TemplateResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import java.io.IOException;
import javax.mail.Session;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class DettaglioOrdine extends BaseController {

    private void action_default(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException, ServletException, TemplateManagerException, DataException {
        TemplateResult res = new TemplateResult(getServletContext());
        request.setAttribute("page_title", "Dettaglio Ordine");

        ApplicationDataLayer dl = ((ApplicationDataLayer) request.getAttribute("datalayer"));

        int ordineId = Integer.parseInt(request.getParameter("n"));
        Ordine ordine = dl.getOrdineDAO().getOrdine(ordineId);
        request.setAttribute("ordine", ordine);

        Utente ordinante = ordine.getProposta().getRichiesta().getOrdinante();
        Utente tecnico = ordine.getProposta().getRichiesta().getTecnico();
        request.setAttribute("ordinante", ordinante);
        request.setAttribute("tecnico", tecnico);    
        

        
        Utente utente = dl.getUtenteDAO().getUtente(userId);
        request.setAttribute("user", utente);

        boolean showAccettaRifiutaButtons = utente.getTipologiaUtente().equals(TipologiaUtente.ORDINANTE)
                && ordine.getStato().equals(StatoOrdine.IN_ATTESA)
                && ordine.getProposta().getRichiesta().getOrdinante().getId() == utente.getId();
        request.setAttribute("showAccettaRifiutaButtons", showAccettaRifiutaButtons);

        boolean accettato = ordine.getStato().equals(StatoOrdine.ACCETTATO);
        request.setAttribute("accettato", accettato);

        res.activate("dettaglioOrdine.ftl.html", request, response);
    }

    private void action_accettaOrdine(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException, TemplateManagerException, DataException {
        int ordineId = SecurityHelpers.checkNumeric(request.getParameter("n"));
        Ordine ordine = ((ApplicationDataLayer) request.getAttribute("datalayer")).getOrdineDAO().getOrdine(ordineId);
        int loggedUserId = (int) request.getSession(false).getAttribute("userid");

        if (ordine.getProposta().getRichiesta().getOrdinante().getId() != loggedUserId) {
            response.sendRedirect("elencoOrdini?error=Non+sei+l'autore+della+richiesta");
            return;
        }

        ordine.setStato(StatoOrdine.ACCETTATO);
        Richiesta richiesta = ordine.getProposta().getRichiesta();
        richiesta.setStato(StatoRichiesta.RISOLTA);
        ((ApplicationDataLayer) request.getAttribute("datalayer")).getRichiestaOrdineDAO().storeRichiesta(richiesta);
        ((ApplicationDataLayer) request.getAttribute("datalayer")).getOrdineDAO().storeOrdine(ordine);

        Utente tecnico = richiesta.getTecnico();
        if (tecnico != null && tecnico.getEmail() != null) {
            Session emailSession = EmailSender.getEmailSession();
            String subject = "Ordine Accettato";
            String body = "<h1>Notifica Accettazione Ordine</h1>"
                    + "<p>L'ordine per la richiesta con codice <strong>"
                    + richiesta.getCodiceRichiesta() + "</strong> è stato accettato.</p>";
            EmailSender.sendEmail(emailSession, tecnico.getEmail(), subject, body);
        }
        response.sendRedirect("elencoOrdini?success=Ordine+accettato+con+successo");
    }

    private void action_rifiutaOrdine(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException, TemplateManagerException, DataException {
        int ordineId = SecurityHelpers.checkNumeric(request.getParameter("n"));
        Ordine ordine = ((ApplicationDataLayer) request.getAttribute("datalayer")).getOrdineDAO().getOrdine(ordineId);
        int loggedUserId = (int) request.getSession(false).getAttribute("userid");

        if (ordine.getProposta().getRichiesta().getOrdinante().getId() != loggedUserId) {
            response.sendRedirect("elencoOrdini?error=Non+sei+l'autore+della+richiesta");
            return;
        }

        String tipoRifiuto = request.getParameter("tipoRifiuto");
        if ("nonConforme".equals(tipoRifiuto)) {
            ordine.setStato(StatoOrdine.RESPINTO_NON_CONFORME);
        } else if ("nonFunzionante".equals(tipoRifiuto)) {
            ordine.setStato(StatoOrdine.RESPINTO_NON_FUNZIONANTE);
        } else {
            ordine.setStato(StatoOrdine.RIFIUTATO);
        }

        Richiesta richiesta = ordine.getProposta().getRichiesta();
        richiesta.setStato(StatoRichiesta.RISOLTA);
        ((ApplicationDataLayer) request.getAttribute("datalayer")).getRichiestaOrdineDAO().storeRichiesta(richiesta);
        ((ApplicationDataLayer) request.getAttribute("datalayer")).getOrdineDAO().storeOrdine(ordine);

        Utente tecnico = richiesta.getTecnico();
        if (tecnico != null && tecnico.getEmail() != null) {
            Session emailSession = EmailSender.getEmailSession();
            String subject = "Ordine Rifiutato";
            String body = "<h1>Notifica Rifiuto Ordine</h1>"
                    + "<p>L'ordine per la richiesta con codice <strong>"
                    + richiesta.getCodiceRichiesta() + "</strong> è stato rifiutato.</p>";
            EmailSender.sendEmail(emailSession, tecnico.getEmail(), subject, body);
        }
        response.sendRedirect("elencoOrdini?error=Ordine+rifiutato");
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
            String action = request.getParameter("action");

            if ("accettaOrdine".equals(action)) {
                action_accettaOrdine(request, response);
            } else if ("rifiutaOrdine".equals(action)) {
                action_rifiutaOrdine(request, response);
            } else {
                action_default(request, response, userId);
            }
        } catch (IOException | TemplateManagerException | DataException ex) {
            handleError(ex, request, response);
        }
    }

    @Override
    public String getServletInfo() {
        return "Dettaglio ordine servlet";
    }
}
