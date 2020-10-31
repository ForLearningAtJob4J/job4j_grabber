package ru.job4j.grabber;

import java.time.LocalDateTime;

public class Post {
    private int id;
    private String name;
    private String text;
    private String link;
    private String authorLink;
    private LocalDateTime created;

    public Post(String name, String text, String link, String authorLink, LocalDateTime created) {
        this.name = name;
        this.text = text;
        this.link = link;
        this.authorLink = authorLink;
        this.created = created;
    }

    @Override
    public String toString() {
        return "Post detail:{" + System.lineSeparator()
                + "name='" + name + "'," + System.lineSeparator()
                + "text='" + text +  "'," + System.lineSeparator()
                + "link='" + authorLink +  "'," + System.lineSeparator()
                + "authorLink='" + authorLink +  "'," + System.lineSeparator()
                + "created=" + created + System.lineSeparator()
                + '}' + System.lineSeparator();
    }
}
