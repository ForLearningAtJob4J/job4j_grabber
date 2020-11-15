package ru.job4j.html;

import ru.job4j.model.Post;

import java.time.LocalDateTime;
import java.util.List;

public interface Parse {
    List<Post> list(LocalDateTime until);

    Post detail(String link);
}