package it.univaq.f4i.iw.ex.webmarket.controller;

import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.ex.webmarket.data.model.Caratteristica;
import it.univaq.f4i.iw.ex.webmarket.data.model.Categoria;
import it.univaq.f4i.iw.ex.webmarket.data.model.Richiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.TipologiaUtente;
import it.univaq.f4i.iw.ex.webmarket.data.model.CaratteristicaRichiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.Utente;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.result.TemplateResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import it.univaq.f4i.iw.framework.result.TemplateManagerException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.mail.Session;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreaRichiesta extends BaseController {

    // Mostra la pagina iniziale con le categorie principali
    private void action_default(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException, ServletException, TemplateManagerException, DataException {
        request.setAttribute("categorie",
                ((ApplicationDataLayer) request.getAttribute("datalayer"))
                        .getCategoriaDAO().getMainCategorie());

        TemplateResult r = new TemplateResult(getServletContext());
        Utente u = ((ApplicationDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtente(userId);
        request.setAttribute("user", u);
        request.setAttribute("page_title", "Nuova Richiesta");
        r.activate("creaRichiesta.ftl.html", request, response);
    }

    // action per caricare le sottocategorie (tramite AJAX)
    private void action_getSubcategories(HttpServletRequest request, HttpServletResponse response)
            throws DataException, IOException {
        int parentCategoryId = SecurityHelpers.checkNumeric(request.getParameter("parentCategoryId"));
        List<Categoria> subcategories = ((ApplicationDataLayer) request.getAttribute("datalayer"))
                .getCategoriaDAO().getCategorieByPadre(parentCategoryId);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(subcategories));
    }

    // action per caricare le caratteristiche (tramite AJAX)
    private void action_getCaratteristiche(HttpServletRequest request, HttpServletResponse response)
            throws DataException, IOException {
        int subcategoryId = SecurityHelpers.checkNumeric(request.getParameter("subcategoryId"));
        List<Caratteristica> caratteristiche = ((ApplicationDataLayer) request.getAttribute("datalayer"))
                .getCaratteristicaDAO().getCaratteristicheByCategoria(subcategoryId);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(caratteristiche));
    }


    // Crea la richiesta e le CaratteristicaRichiesta associate (POST)
    private void action_creaRichiesta(HttpServletRequest request, HttpServletResponse response, int userId)
            throws DataException, IOException {

        try {
            Utente utente = ((ApplicationDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtente(userId);

            Richiesta richiesta = ((ApplicationDataLayer) request.getAttribute("datalayer"))
                    .getRichiestaOrdineDAO().createRichiesta();

            richiesta.setNote(request.getParameter("note"));
            richiesta.setStato(it.univaq.f4i.iw.ex.webmarket.data.model.StatoRichiesta.IN_ATTESA);
            richiesta.setData(new Timestamp(new Date().getTime()));
            richiesta.setOrdinante(utente);
            // richiesta.setTecnico(null); // Il tecnico verrà assegnato in un secondo momento (opzionale, ma consigliato)
            int subcategoryId = Integer.parseInt(request.getParameter("subcategoryId"));
            Categoria categoria = ((ApplicationDataLayer) request.getAttribute("datalayer"))
                    .getCategoriaDAO().getCategoria(subcategoryId);
            richiesta.setCategoria(categoria);

            ((ApplicationDataLayer) request.getAttribute("datalayer"))
                    .getRichiestaOrdineDAO().storeRichiesta(richiesta);

            // Crea gli oggetti CaratteristicaRichiesta
            List<Caratteristica> caratteristiche = ((ApplicationDataLayer) request.getAttribute("datalayer"))
                    .getCaratteristicaDAO().getCaratteristicheByCategoria(subcategoryId);

            for (Caratteristica caratteristica : caratteristiche) {
                String valoreCaratteristica = request.getParameter("caratteristica-" + caratteristica.getKey());
                if (valoreCaratteristica != null && !valoreCaratteristica.isEmpty()) {
                    CaratteristicaRichiesta caratteristicaRichiesta = ((ApplicationDataLayer) request.getAttribute("datalayer"))
                            .getCaratteristicaRichiestaDAO().createCR();
                    caratteristicaRichiesta.setRichiesta(richiesta);
                    caratteristicaRichiesta.setCaratteristica(caratteristica);
                    caratteristicaRichiesta.setValore(valoreCaratteristica);
                    ((ApplicationDataLayer) request.getAttribute("datalayer"))
                            .getCaratteristicaRichiestaDAO().storeCR(caratteristicaRichiesta);
                }
            }

            // *** Invio email a tutti i tecnici 
            try {
                List<Utente> tecnici = ((ApplicationDataLayer) request.getAttribute("datalayer"))
                        .getUtenteDAO().getAllByRole(TipologiaUtente.TECNICO);

                if (tecnici != null && !tecnici.isEmpty()) {
                    Session emailSession = EmailSender.getEmailSession();
                    String subject = "Nuova Richiesta in Attesa";
                    String body = "<h1>Nuova Richiesta</h1>"
                                + "<p>È stata creata una nuova richiesta con codice <strong>" + richiesta.getCodiceRichiesta() + "</strong>.</p>"
                                + "<p>Controlla la sezione richieste per prenderla in carico!.</p>";

                    for (Utente tecnico : tecnici) {
                        if (tecnico.getEmail() != null && !tecnico.getEmail().isEmpty()) {
                            EmailSender.sendEmail(emailSession, tecnico.getEmail(), subject, body);
                        }
                    }
                }
            } catch (Exception e) {
                // Gestisci eventuali errori nell'invio delle email.  
                Logger.getLogger(CreaRichiesta.class.getName()).log(Level.SEVERE, "Errore durante l'invio dell'email ai tecnici", e);
            }

            // SUCCESS: Reindirizza con messaggio di successo
            response.sendRedirect("elencoRichieste?success=" + URLEncoder.encode("Richiesta creata con successo!", "UTF-8"));

        } catch (Exception ex) {
            
        
            Logger.getLogger(CreaRichiesta.class.getName()).log(Level.SEVERE, "Errore durante la creazione della richiesta", ex);


            // Reindirizza con messaggio di errore
            response.sendRedirect("elencoRichieste?error=" + URLEncoder.encode("Si è verificato un errore durante la creazione della richiesta: " + ex.getMessage(), "UTF-8"));
            
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

            String action = request.getParameter("action");
            if ("creaRichiesta".equalsIgnoreCase(action) && "POST".equalsIgnoreCase(request.getMethod())) {
                action_creaRichiesta(request, response,userId);
            } else if ("getSubcategories".equalsIgnoreCase(action)) {
                action_getSubcategories(request, response);
            } else if ("getCaratteristiche".equalsIgnoreCase(action)) {
                action_getCaratteristiche(request, response);
            } else {
                action_default(request, response,userId);
            }
        } catch (IOException | TemplateManagerException | DataException ex) {
            Logger.getLogger(CreaRichiesta.class.getName()).log(Level.SEVERE, null, ex);
            handleError(ex, request, response);
        }
    }

    @Override
    public String getServletInfo() {
        return "Controller per la pagina Nuova Richiesta";
    }
}
