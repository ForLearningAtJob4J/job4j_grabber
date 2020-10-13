package ru.job4j.model;

import java.time.LocalDateTime;

public class Post {
    private String header;
    private String text;
    private String author;
    private LocalDateTime created;

    public Post(String header, String text, String author, LocalDateTime created) {
        this.header = header;
        this.text = text;
        this.author = author;
        this.created = created;
    }

    @Override
    public String toString() {
        return "Post detail:{" + System.lineSeparator()
                + "header='" + header + '\'' + System.lineSeparator()
                + ", text='" + text + '\'' + System.lineSeparator()
                + ", author='" + author + '\'' + System.lineSeparator()
                + ", created=" + created + System.lineSeparator()
                + '}' + System.lineSeparator();
    }
}
