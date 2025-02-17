
package it.univaq.f4i.iw.ex.webmarket.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Session;
import javax.servlet.ServletException;
import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.ex.webmarket.data.model.Utente;
import it.univaq.f4i.iw.ex.webmarket.data.model.TipologiaUtente;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.UtenteImpl;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.result.TemplateManagerException;
import it.univaq.f4i.iw.framework.result.TemplateResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;



public class AggiungiUtente extends BaseController {

    private void action_default(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, TemplateManagerException {
        TemplateResult res = new TemplateResult(getServletContext());
        request.setAttribute("page_title", "Gestione Utenti");
        res.activate("aggiungiUtente.ftl.html", request, response);
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

        String action = request.getParameter("action");
        if (action != null && action.equals("createUser")) {
            action_creaUtente(request, response);
        } else {
            action_default(request, response);
        }
    } else {
        response.sendRedirect("login");
    }
    } catch (IOException | TemplateManagerException | DataException ex) {
        handleError(ex, request, response);
    } catch (NoSuchAlgorithmException ex) {
        Logger.getLogger(AggiungiUtente.class.getName()).log(Level.SEVERE, null, ex);
    } catch (InvalidKeySpecException ex) {
        Logger.getLogger(AggiungiUtente.class.getName()).log(Level.SEVERE, null, ex);
    }
}

    private void action_creaUtente(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, DataException, TemplateManagerException, NoSuchAlgorithmException, InvalidKeySpecException {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("temp-password");
        String confirmPassword = request.getParameter("confirm-password");
        String roleParam = request.getParameter("role");
        
        if (username == null || email == null || password == null || confirmPassword == null || roleParam == null) {
            response.sendRedirect("aggiungiUtente?error=Tutti+i+campi+sono+obbligatori");
            return;
        }

        if (!password.equals(confirmPassword)) {
            
            response.sendRedirect("aggiungiUtente?error=Le+password+non+coincidono");
            return;
        }
        
        // controllo se lo username esiste già nel database
        Utente existingUser = ((ApplicationDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtenteByUsername(username);

        if (existingUser != null) {
            
            response.sendRedirect("aggiungiUtente?error=username+utilizzato");

            return;
        }

        TipologiaUtente role = TipologiaUtente.valueOf(roleParam.toUpperCase());
        String hashedPass = SecurityHelpers.getPasswordHashPBKDF2(password);

        Utente nuovoUtente = new UtenteImpl();
        nuovoUtente.setUsername(username);
        nuovoUtente.setEmail(email);
        nuovoUtente.setPassword(hashedPass);
        nuovoUtente.setTipologiaUtente(role);

        ((ApplicationDataLayer) request.getAttribute("datalayer")).getUtenteDAO().storeUtente(nuovoUtente);
        try {
            
            Session emailSession = EmailSender.getEmailSession();


            String subject = "Benvenuto in Coquette";
            String body = "Ciao e Benvenuto in Coquette,\n\n" +
                        "Ecco le tue credenziali per accedere:\n" +
                        "Username: " + username + "\n" +
                        "Password : " + password ;
            
            EmailSender.sendEmail(emailSession, email, subject, body);
            response.sendRedirect("aggiungiUtente?success=Utente+creato+con+successo+e+email+inviata");
        } catch (Exception e) {
            response.sendRedirect("aggiungiUtente?success=Utente+creato+con+successo+ma+si+è+verificato+un+problema+con+la+email");
                e.printStackTrace();
            }
            response.sendRedirect("aggiungiUtente?success=Utente+creato+con+successo");
        action_default(request, response);
    }


    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "servlet per la gestione degli utenti(creazione) da parte dell'admin";
    }
}