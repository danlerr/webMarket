package it.univaq.f4i.iw.ex.newspaper.data.model.impl;

import it.univaq.f4i.iw.ex.newspaper.data.model.Article;
import it.univaq.f4i.iw.ex.newspaper.data.model.Author;
import it.univaq.f4i.iw.ex.newspaper.data.model.Image;
import it.univaq.f4i.iw.ex.newspaper.data.model.Issue;
import it.univaq.f4i.iw.framework.data.DataItemImpl;
import java.util.List;

public class ArticleImpl extends DataItemImpl<Integer> implements Article {

    private Author author;
    private String text;
    private String title;
    private List<Image> images;
    private Issue issue;
    private int page;

    public ArticleImpl() {
        super();
        author = null;
        text = "";
        title = "";
        images = null;
        issue = null;
        page = 0;

    }

    @Override
    public Author getAuthor() {
        return author;
    }

    @Override
    public void setAuthor(Author author) {
        this.author = author;

    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public List<Image> getImages() {
        return images;
    }

    @Override
    public void setImages(List<Image> images) {
        this.images = images;
    }

    @Override
    public void addImage(Image image) {
        this.images.add(image);
    }

    @Override
    public boolean isPublished() {
        return (getIssue() != null);
    }

    @Override
    public Issue getIssue() {
        return issue;
    }

    @Override
    public void setIssue(Issue issue) {
        this.issue = issue;

    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public void setPage(int page) {
        this.page = page;
    }

}
