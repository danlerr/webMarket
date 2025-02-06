package it.univaq.f4i.iw.ex.webmarket.controller;

import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.ex.webmarket.data.model.Utente;
import it.univaq.f4i.iw.framework.result.TemplateManagerException;
import it.univaq.f4i.iw.framework.result.TemplateResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class HomeAdmin extends BaseController {

    private void action_default(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, TemplateManagerException, DataException {
        TemplateResult r = new TemplateResult(getServletContext());
        request.setAttribute("page_title", "Admin Dashboard");

        List<Utente> utenti = ((ApplicationDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getAll();
        request.setAttribute("utenti", utenti); 

        r.activate("homeadmin.ftl.html", request, response);
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
        Utente u = ((ApplicationDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtente(userId);
        
        if (u != null) {
            request.setAttribute("user", u);
        }
        action_default(request, response);
    } catch (IOException | TemplateManagerException | DataException ex) {
        handleError(ex, request, response);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Servlet per la home dell'admin";
    }
}