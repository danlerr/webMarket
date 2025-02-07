package it.univaq.f4i.iw.ex.webmarket.controller;

import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.ex.webmarket.data.model.PropostaAcquisto;
import it.univaq.f4i.iw.ex.webmarket.data.model.RichiestaOrdine;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.PropostaAcquistoImpl;
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
import com.itextpdf.text.DocumentException;


public class InvioProposta extends BaseController {

    private void action_default(HttpServletRequest request, HttpServletResponse response, int n) throws IOException, ServletException, TemplateManagerException, DataException {
        TemplateResult res = new TemplateResult(getServletContext());
        request.setAttribute("page_title", "Invio Proposta");

        //Recupero la key passata come parametro
        int richiesta_key = Integer.parseInt(request.getParameter("n"));
        
        //Recupero la richiesta usando la key
        Richiesta richiesta = ((ApplicationDataLayer) request.getAttribute("datalayer")).getRichiestaDAO().getRichiesta(richiesta_key);
        request.setAttribute("richiesta", richiesta);

        res.activate("invioProposta.ftl.html", request, response);
    }

    private void action_sendProposta(HttpServletRequest request, HttpServletResponse response, int n) throws IOException, ServletException, TemplateManagerException, DataException {
        Richiesta richiesta = ((ApplicationDataLayer) request.getAttribute("datalayer")).getRichiestaDAO().getRichiesta(n);

        // Recupero i dati dal form
        String produttore = request.getParameter("produttore");
        String prodotto = request.getParameter("prodotto");
        String codiceProdotto = request.getParameter("codiceProdotto");
        float prezzo;
        try {
            prezzo = Float.parseFloat(request.getParameter("prezzo"));
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "Il prezzo deve essere un valore numerico valido.");
            action_default(request, response, n);
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
        PropostaAcquisto proposta = new PropostaImpl();
        proposta.setProduttore(produttore);
        proposta.setProdotto(prodotto);
        proposta.setCodiceProdotto(codiceProdotto);
        proposta.setPrezzo(prezzo);
        proposta.setUrl(url);
        proposta.setNote(note);
        proposta.setStatoProposta(StatoProposta.IN_ATTESA);
        proposta.setMotivazione(null);
        proposta.setRichiesta(richiesta);

        // Salvo la proposta nel database
        ((ApplicationDataLayer) request.getAttribute("datalayer")).getPropostaDAO().storeProposta(proposta);

        

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
                action_sendProposta(request, response, n);
            } else {
                action_default(request, response, n);
            }

        } catch (IOException | TemplateManagerException ex) {
            handleError(ex, request, response);
        } catch (DataException ex) {
            Logger.getLogger(InvioProposta.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "Servlet per l'invio di una nuova proposta d'acquisto";
    }
}