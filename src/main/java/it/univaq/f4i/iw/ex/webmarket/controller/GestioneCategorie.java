package it.univaq.f4i.iw.ex.webmarket.controller;

import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.ex.webmarket.data.model.Categoria;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.result.TemplateManagerException;
import it.univaq.f4i.iw.framework.result.TemplateResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class GestioneCategorie extends BaseController {
    
    /**
     * Azione di default: mostra la pagina di gestione categorie.
     * Inizialmente vengono mostrate solo le categorie padre.
     */
    private void action_default(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException, TemplateManagerException, DataException {
        
        TemplateResult res = new TemplateResult(getServletContext());
        request.setAttribute("page_title", "Gestione Categorie");
        
        // Recupera tutte le categorie dal DAO.
        // Si consiglia di avere un metodo getCategoriePadre() che restituisca solo le categorie senza padre
        // oppure, se non disponibile, recuperare tutte le categorie e filtrarle nella view.
        List<Categoria> categoriePadre = ((ApplicationDataLayer) request.getAttribute("datalayer"))
                .getCategoriaDAO().getCategoriePadre();
        request.setAttribute("categoriePadre", categoriePadre);
        
        // Il template potrà richiamare (via JavaScript) un endpoint per recuperare le sotto-categorie di un determinato padre
        // oppure, se preferisci, puoi passare tutte le categorie e fare il filtraggio lato client.
        
        res.activate("gestione_categorie.ftl.html", request, response);
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
            
            // Qui si potrebbe leggere un parametro "action" per distinguere eventuali operazioni (ad es. aggiunta di categoria o caratteristica)
            String action = request.getParameter("action");
            if (action != null) {
                // Esempio: se l'azione è "addCategory", si può chiamare una funzione che apre il form di inserimento.
                // Oppure se è "addFeature", si apre un form per aggiungere una caratteristica alla sotto-categoria.
                // Nel seguente esempio, per semplicità, mostriamo solo la visualizzazione di default.
                // Puoi gestire più azioni in base alle tue esigenze.
            }
            
            action_default(request, response);
            
        } catch (IOException | TemplateManagerException | DataException ex) {
            handleError(ex, request, response);
        }
    }
    
    @Override
    public String getServletInfo() {
        return "Servlet per la gestione delle categorie ad albero (categorie padre e sotto-categorie)";
    }
}
//creacategoria
//eliminacategoria