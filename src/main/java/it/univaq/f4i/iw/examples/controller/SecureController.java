/*
 * TestMyDAO.java
 *
 *
 */
package it.univaq.f4i.iw.examples.controller;

import it.univaq.f4i.iw.ex.newspaper.data.model.Article;
import it.univaq.f4i.iw.ex.newspaper.data.model.Author;
import it.univaq.f4i.iw.ex.newspaper.data.model.Issue;
import it.univaq.f4i.iw.examples.application.ApplicationDataLayer;
import it.univaq.f4i.iw.examples.application.ApplicationBaseController;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import it.univaq.f4i.iw.framework.view.TemplateManagerException;
import it.univaq.f4i.iw.framework.view.TemplateResult;
import java.io.IOException;
import java.time.LocalDate;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Ingegneria del Web
 * @version
 */
public class SecureController extends ApplicationBaseController {

    private void action_default(HttpServletRequest request, HttpServletResponse response) throws IOException, DataException, TemplateManagerException {

        //preleviamo il data layer 
        //get the data layer
        ApplicationDataLayer dl = (ApplicationDataLayer) request.getAttribute("datalayer");

        //manipoliamo i dati usando le interfacce esposta dai DAO accessibili dal data layer
        //manipulate the data using the interfaces exposed by the DAOs accessible from the data layer
        Issue old_latest_issue = dl.getIssueDAO().getLatestIssue();
        //        
        Issue new_issue = dl.getIssueDAO().createIssue();
        new_issue.setNumber((old_latest_issue.getNumber() + 1));
        new_issue.setDate(LocalDate.now());
        dl.getIssueDAO().storeIssue(new_issue);
        //        
        Article new_article = dl.getArticleDAO().createArticle();
        Author author = dl.getAuthorDAO().getAuthor(1); //assume it already exists
        if (author != null) {
            new_article.setAuthor(author);
            new_article.setTitle(SecurityHelpers.addSlashes("NEW ARTICLE FOR ISSUE " + (old_latest_issue.getNumber() + 1)));
            new_article.setText(SecurityHelpers.addSlashes("article text"));
            new_article.setIssue(new_issue);
            dl.getArticleDAO().storeArticle(new_article);
            //
            Issue new_latest_issue = dl.getIssueDAO().getLatestIssue();

            TemplateResult result = new TemplateResult(getServletContext());
            request.setAttribute("page_title", "Manipulation");
            request.setAttribute("oldLatestIssue", old_latest_issue);
            request.setAttribute("newIssue", new_issue);
            request.setAttribute("newArticle", new_article);
            request.setAttribute("newLatestIssue", new_latest_issue);

            result.activate("secure.ftl.html", request, response);
        } else {
            handleError("Cannot add article: undefined author", request, response);
        }
    }

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        action_default(request, response);
    }
}
