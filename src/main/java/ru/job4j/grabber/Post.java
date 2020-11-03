package ru.job4j.grabber;

import java.time.LocalDateTime;

public class Post {
    private String id;
    private String name;
    private String text;
    private String link;
    private String authorLink;
    private LocalDateTime created;

    public Post(String id, String name, String text, String link, String authorLink, LocalDateTime created) {
        this.id = id;
        this.name = name;
        this.text = text;
        this.link = link;
        this.authorLink = authorLink;
        this.created = created;
    }

    public Post(String name, String text, String link, String authorLink, LocalDateTime created) {
        this("0", name, text, link, authorLink, created);
    }

    public Post setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public String getLink() {
        return link;
    }

    public String getAuthorLink() {
        return authorLink;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    @Override
    public String toString() {
        return "Post detail:{" + System.lineSeparator()
                + "id='" + id + "'," + System.lineSeparator()
                + "name='" + name + "'," + System.lineSeparator()
                + "text='" + text +  "'," + System.lineSeparator()
                + "link='" + link +  "'," + System.lineSeparator()
                + "authorLink='" + authorLink +  "'," + System.lineSeparator()
                + "created=" + created + System.lineSeparator()
                + '}' + System.lineSeparator();
    }

    public String getId() {
        return id;
    }
}
