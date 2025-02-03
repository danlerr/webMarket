package it.univaq.f4i.iw.ex.webmarket.controller;

import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.ex.webmarket.data.model.Categoria;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.CategoriaImpl;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.result.SplitSlashesFmkExt;
import it.univaq.f4i.iw.framework.result.TemplateManagerException;
import it.univaq.f4i.iw.framework.result.TemplateResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet per la creazione di una nuova categoria.
 * Questa servlet gestisce sia la visualizzazione della pagina per creare una categoria
 * (action_default) sia la creazione vera e propria della categoria (action_createCategory).
 */
public class CreaCategoria extends BaseController {
    
    /**
     * Metodo di default: mostra la pagina per la creazione di una categoria.
     * Carica nella request l'elenco di tutte le categorie esistenti e imposta il titolo della pagina.
     * Utilizza il template "aggiungi_categoria.ftl.html".
     */
    private void action_default(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException, TemplateManagerException, DataException {
        try {
            // Recupera l'elenco di tutte le categorie dal DAO e lo imposta nella request
            request.setAttribute("categorie", ((ApplicationDataLayer) request.getAttribute("datalayer"))
                    .getCategoriaDAO().getAllCategorie());
            
            // Imposta il titolo della pagina
            request.setAttribute("page_title", "Crea categoria");
            
            // Crea l'oggetto TemplateResult per gestire il rendering della view
            TemplateResult res = new TemplateResult(getServletContext());
            
            // Imposta nella request un helper per la gestione degli slash (da usare nel template)
            request.setAttribute("strip_slashes", new SplitSlashesFmkExt());
            
            // Attiva il template "aggiungi_categoria.ftl.html" per la visualizzazione
            res.activate("aggiungi_categoria.ftl.html", request, response);
        } catch (DataException ex) {
            // Gestisce eventuali errori di accesso ai dati e mostra un messaggio di errore
            handleError("Data access exception: " + ex.getMessage(), request, response);
        }
    }
    
    /**
     * Metodo processRequest:
     * Verifica la sessione utente e, in base al parametro "action", decide se creare una categoria o mostrare la pagina.
     */
    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        try {
            // Controlla che la sessione sia valida; se non lo è, reindirizza al login
            HttpSession session = SecurityHelpers.checkSession(request);
            if (session == null) {
                response.sendRedirect("login");
                return;
            }
            
            // Controlla se esiste il parametro "action" nella request
            String action = request.getParameter("action");
            if (action != null && action.equals("createCategory")) {
                // Se l'azione è "createCategory", invoca il metodo per creare la categoria
                action_createCategory(request, response);
            } else {
                // Altrimenti mostra la pagina di default
                action_default(request, response);
            }
        } catch (IOException | TemplateManagerException | DataException ex) {
            // Gestisce eventuali errori di I/O, di template o di accesso ai dati
            handleError(ex, request, response);
        }
    }
    
    /**
     * Metodo per la creazione di una nuova categoria.
     * Legge i parametri dalla request, crea un oggetto Categoria, lo memorizza nel database
     * e reindirizza l'utente alla pagina della categoria appena creata.
     */
    private void action_createCategory(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException, DataException, TemplateManagerException {
        // Recupera il nome della categoria dal parametro "category-name"
        String name = request.getParameter("category-name");
        // Recupera l'id della categoria padre dal parametro "parent-category" e lo converte in intero
        int parent = Integer.parseInt(request.getParameter("parent-category"));
        
        // Crea un'istanza di CategoriaImpl e imposta i valori
        Categoria categoria = new CategoriaImpl();
        categoria.setNome(name);
        categoria.setPadre(parent);
        
        // Salva la nuova categoria nel database attraverso il DAO
        ((ApplicationDataLayer) request.getAttribute("datalayer")).getCategoriaDAO().storeCategoria(categoria);
        
        // Recupera l'id generato per la nuova categoria
        int categoriaId = categoria.getKey();
        
        // Prepara l'URL per il redirect alla pagina della categoria appena creata
        String redirectUrl = "categoria?n=" + categoriaId;
        response.sendRedirect(redirectUrl);
    }
    
    /**
     * Restituisce una breve descrizione della servlet.
     */
    @Override
    public String getServletInfo() {
        return "Categoria servlet";
    }
}
