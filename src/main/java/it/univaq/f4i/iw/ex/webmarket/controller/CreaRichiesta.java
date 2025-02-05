package it.univaq.f4i.iw.ex.webmarket.controller;

import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.ex.webmarket.data.model.Caratteristica;
import it.univaq.f4i.iw.ex.webmarket.data.model.Categoria;
import it.univaq.f4i.iw.ex.webmarket.data.model.Richiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.CaratteristicaRichiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.Utente;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.result.TemplateResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import it.univaq.f4i.iw.framework.result.TemplateManagerException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreaRichiesta extends BaseController {

    // Mostra la pagina iniziale con le categorie radice
    private void action_default(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException, TemplateManagerException, DataException {
        request.setAttribute("categorie",
                ((ApplicationDataLayer) request.getAttribute("datalayer"))
                        .getCategoriaDAO().getMainCategorie());

        TemplateResult r = new TemplateResult(getServletContext());
        request.setAttribute("page_title", "Nuova Richiesta");
        r.activate("creaRichiesta.ftl.html", request, response);
    }

    // Carica le sottocategorie tramite AJAX
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

    // Carica le caratteristiche tramite AJAX
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
    private void action_creaRichiesta(HttpServletRequest request, HttpServletResponse response) throws DataException, IOException {
        HttpSession session = request.getSession(false); // Ottieni la sessione senza crearne una nuova
        if (session == null || session.getAttribute("utente") == null) {
          response.sendRedirect("login"); // o gestisci come preferisci l'utente non autenticato
          return;
        }
        
        Utente utente = (Utente) session.getAttribute("utente"); // Recupera l'utente dalla sessione
        // 1. Crea l'oggetto Richiesta
        Richiesta richiesta = ((ApplicationDataLayer) request.getAttribute("datalayer")).getRichiestaOrdineDAO().createRichiesta();
        
        richiesta.setNote(request.getParameter("note"));
        richiesta.setStato(it.univaq.f4i.iw.ex.webmarket.data.model.StatoRichiesta.IN_ATTESA); // Imposta lo stato iniziale
        richiesta.setData(new Timestamp(new Date().getTime())); // Imposta la data corrente

        // Genera un codice univoco (esempio semplice, da migliorare)
        String codiceRichiesta = "REQ-" + System.currentTimeMillis();
        richiesta.setCodiceRichiesta(codiceRichiesta);

        richiesta.setOrdinante(utente); // Imposta l'utente dalla sessione
        //richiesta.setTecnico(); // Il tecnico verrà assegnato in un secondo momento
        int subcategoryId = Integer.parseInt(request.getParameter("subcategoryId"));

        Categoria categoria = ((ApplicationDataLayer) request.getAttribute("datalayer")).getCategoriaDAO().getCategoria(subcategoryId);
        richiesta.setCategoria(categoria); // Imposta la categoria
        ((ApplicationDataLayer) request.getAttribute("datalayer")).getRichiestaOrdineDAO().storeRichiesta(richiesta); // Salva la richiesta

        // 2. Crea gli oggetti CaratteristicaRichiesta
        List<Caratteristica> caratteristiche = ((ApplicationDataLayer) request.getAttribute("datalayer")).getCaratteristicaDAO().getCaratteristicheByCategoria(subcategoryId);

        for (Caratteristica caratteristica : caratteristiche) {
            String valoreCaratteristica = request.getParameter("caratteristica-" + caratteristica.getKey()); // Recupera il valore dal form
            if(valoreCaratteristica != null && !valoreCaratteristica.isEmpty()){ //Controlla che il valore della caratteristica non sia nullo
                CaratteristicaRichiesta caratteristicaRichiesta = ((ApplicationDataLayer) request.getAttribute("datalayer")).getCaratteristicaRichiestaDAO().createCR();
                caratteristicaRichiesta.setRichiesta(richiesta); // Collega alla richiesta
                caratteristicaRichiesta.setCaratteristica(caratteristica); // Collega alla caratteristica
                caratteristicaRichiesta.setValore(valoreCaratteristica);  // Imposta il valore
                ((ApplicationDataLayer) request.getAttribute("datalayer")).getCaratteristicaRichiestaDAO().storeCR(caratteristicaRichiesta); // Salva
            }
         }

        response.sendRedirect("successPage"); // Reindirizza a una pagina di successo (o dove preferisci)
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

            String action = request.getParameter("action");
            if ("creaRichiesta".equalsIgnoreCase(action) && "POST".equalsIgnoreCase(request.getMethod())) {
                action_creaRichiesta(request, response);
            } else if ("getSubcategories".equalsIgnoreCase(action)) {
                action_getSubcategories(request, response);
            } else if ("getCaratteristiche".equalsIgnoreCase(action)) {
                action_getCaratteristiche(request, response);
            } else {
                action_default(request, response);
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
