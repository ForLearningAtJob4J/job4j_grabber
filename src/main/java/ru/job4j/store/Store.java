package ru.job4j.store;

import ru.job4j.model.Post;

import java.time.LocalDateTime;
import java.util.List;

public interface Store {
    void save(Post post);

    void saveAll(List<Post> posts);

    List<Post> getAll();

    Post findById(String id);

    LocalDateTime getMaxDate();
}