package ru.job4j.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.model.Post;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(PsqlStore.class);
    private static final String PROPERTY_FILE_NAME = "app.properties";

    private final Connection cnn;

    public PsqlStore(Properties cfg) throws SQLException {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        cnn = DriverManager.getConnection(
                cfg.getProperty("jdbc.url"),
                cfg.getProperty("jdbc.username"),
                cfg.getProperty("jdbc.password")
        );
    }

    /**
     * Sharpness of timestamp data type field is microseconds, but we know that sql.ru uses only HH:MM.
     * Also, if id was 0 it will be changed on actual
     * @param post - post detail to save
     */
    @Override
    public void save(Post post) {
        try (PreparedStatement st =
                     cnn.prepareStatement("INSERT INTO public.post(\n"
                             + ("0".equals(post.getId()) ? "" : "id, ") + "name, text, link, authorLink, created)\n"
                             + "VALUES (" + ("0".equals(post.getId()) ? "" : post.getId() + ", ")
                             + "?, ?, ?, ?, ?) ON CONFLICT (link) DO NOTHING;", Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, post.getName());
            st.setString(2, post.getText());
            st.setString(3, post.getLink());
            st.setString(4, post.getAuthorLink());
            st.setTimestamp(5, Timestamp.valueOf(post.getCreated()));
            st.executeUpdate();
            ResultSet rs  = st.getGeneratedKeys();
            if ("0".equals(post.getId()) && rs.next()) {
                post.setId(rs.getString(1));
            }
        } catch (SQLException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void saveAll(List<Post> posts) {
        try {
            boolean prevAutoCommit = cnn.getAutoCommit();
            cnn.setAutoCommit(false);
            posts.forEach(this::save);
            cnn.commit();
            cnn.setAutoCommit(prevAutoCommit);
        } catch (SQLException throwable) {
            throwable.printStackTrace();
            LOG.error(throwable.getLocalizedMessage(), throwable);
        }
    }

    @Override
    public List<Post> getAll() {
        try (PreparedStatement st = cnn.prepareStatement("SELECT id, name, text, link, authorLink, created \n"
                + "FROM public.post;")) {
            List<Post> res = new ArrayList<>();
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                Post post = new Post(rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getTimestamp(6).toLocalDateTime());
                res.add(post);
            }
            return res;
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Post findById(String id) {
        try (PreparedStatement st = cnn.prepareStatement("SELECT id, name, text, link, authorLink, created \n"
                + "FROM public.post WHERE id = ?")) {
            st.setInt(1, Integer.parseInt(id));
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return new Post(rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getTimestamp(6).toLocalDateTime());
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    public LocalDateTime getMaxDate() {
        try (PreparedStatement st = cnn.prepareStatement("SELECT max(created) FROM public.post")) {
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                Timestamp ret = rs.getTimestamp(1);
                if (ret != null) {
                    LOG.info(ret.toLocalDateTime().toString());
                    return ret.toLocalDateTime();
                }
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    private static Properties makeCfg() {
        try (InputStream in = PsqlStore.class.getClassLoader().getResourceAsStream(PROPERTY_FILE_NAME)) {
            Properties config = new Properties();
            assert in != null;
            config.load(in);
            return config;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static void main(String[] args) throws SQLException, InterruptedException {
        PsqlStore store = new PsqlStore(makeCfg());
//        store.cnn.setAutoCommit(false);
        Post p = new Post("vacancy1",
                "Bla-bla duties",
                "http://sql.ru/vacancy1" + System.currentTimeMillis(),
                "http://sql.ru/author1",
                LocalDateTime.now());
        store.save(p);
        String id = p.getId();
        Thread.sleep(2000); //just because... and "created" field would be different )
        p = new Post("vacancy2",
                "Bla-bla duties duties duties duties",
                "http://sql.ru/vacancy2" + System.currentTimeMillis(),
                "http://sql.ru/author2",
                LocalDateTime.now());
        store.save(p);
        System.out.println("FIND FIND FIND FIND FIND FIND FIND FIND FIND FIND ");
        System.out.println(store.findById(id));
        System.out.println("GETALL GETALL GETALL GETALL GETALL GETALL GETALL GETALL ");
        store.getAll().forEach(System.out::println);
//        store.cnn.rollback();
    }
}