package it.univaq.f4i.iw.ex.webmarket.controller;

import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.ex.webmarket.data.model.Proposta;
import it.univaq.f4i.iw.ex.webmarket.data.model.Richiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.PropostaImpl;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.StatoProposta;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.result.TemplateManagerException;
import it.univaq.f4i.iw.framework.result.TemplateResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Session;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class CreaProposta extends BaseController {

    private void action_default(HttpServletRequest request, HttpServletResponse response, int id) throws IOException, ServletException, TemplateManagerException, DataException {
        TemplateResult res = new TemplateResult(getServletContext());
        request.setAttribute("page_title", "Crea proposta");

        int richiesta_id = Integer.parseInt(request.getParameter("id"));
        
        //retrieve della richiesta
        Richiesta richiesta = ((ApplicationDataLayer) request.getAttribute("datalayer")).getRichiestaOrdineDAO().getRichiesta(richiesta_id);
        request.setAttribute("richiesta", richiesta);

        res.activate("crea_proposta.ftl.html", request, response);
    }

    private void action_createProposta(HttpServletRequest request, HttpServletResponse response, int id) throws IOException, ServletException, TemplateManagerException, DataException {
        Richiesta richiesta = ((ApplicationDataLayer) request.getAttribute("datalayer")).getRichiestaOrdineDAO().getRichiesta(id);

        String produttore = request.getParameter("produttore");
        String prodotto = request.getParameter("prodotto");
        String codiceProdotto = request.getParameter("codiceProdotto");
        float prezzo;
        try {
            prezzo = Float.parseFloat(request.getParameter("prezzo"));
        } catch (NumberFormatException ex) {
            request.setAttribute("errorMessage", "Il prezzo deve essere un valore numerico valido.");
            action_default(request, response, id);
            return;
        }
        String url = request.getParameter("url");
        String note;
        if (request.getParameter("note").isEmpty()) {
            note = null;
        } else {
            note = request.getParameter("note");
        }

        // Creo una nuova proposta
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

        // Recupero l'email dell'utente
        String email = richiesta.getOrdinante().getEmail();

        // Properties per la mail:
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.outlook.com"); 
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication("webmarket.univaq@outlook.com", "geagiuliasamanta1");
            }
        });

        String tipo = "PropostaRichiesta_";
        String text = "Gentile Utente, Le è stata inviata una proposta d'acquisto per la sua richiesta numero " + richiesta.getCodiceRichiesta() + ". In allegato trova i dettagli.\n\nCordiali Saluti,\nIl team di WebMarket"; 

        String messaggio = "Dettagli della proposta per la richiesta numero: " + richiesta.getCodiceRichiesta() + "\n\n";

        try {
            EmailSender.sendEmailWithAttachment(session, email, "Notifica Proposta", text);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        // Reindirizzo alla pagina di dettaglio della proposta
        response.sendRedirect("detailproposta_tecnico?n=" + proposta.getKey());
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

            String action = request.getParameter("action");
            if (action != null && action.equals("invioProposta")) {
                action_createProposta(request, response, n);
            } else {
                action_default(request, response, n);
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
