package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.grabber.Parse;
import ru.job4j.model.Post;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SqlRuParse implements Parse {

    private static final Logger LOG = LoggerFactory.getLogger(SqlRuParse.class);

    private static final HashMap<String, Integer> MONTHS = new HashMap<>() { {
        put("янв", 1);
        put("фев", 2);
        put("мар", 3);
        put("апр", 4);
        put("май", 5);
        put("июн", 6);
        put("июл", 7);
        put("авг", 8);
        put("сен", 9);
        put("окт", 10);
        put("ноя", 11);
        put("дек", 12);
    } };
    private static final int MAX_NUM_PAGES = 5;
    private static final int BASE_YEAR = 2000;
    private static final String BASE_URL = "https://www.sql.ru/forum/job-offers/";

    private LocalDateTime parseDate(String s) {
        String[] arr = s.split(", ");
        LocalDate ld;
        if ("сегодня".equals(arr[0])) {
            ld = LocalDate.now();
        } else if ("вчера".equals(arr[0])) {
            ld = LocalDate.now().minusDays(1);
        } else {
            String[] splittedDate = arr[0].split(" ");
            ld = LocalDate.of(BASE_YEAR + Integer.parseInt(splittedDate[2]), MONTHS.get(splittedDate[1]),
                    Integer.parseInt(splittedDate[0]));
        }
        return LocalDateTime.of(ld, LocalTime.parse(arr[1]));
    }

    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(link).get();
            Elements rows = doc.select(".forumTable tr");
            for (int i = 1; i < rows.size(); i++) {
                Element tr = rows.get(i);
                Elements nodes = tr.child(1).children();
                boolean isClosed = false;
                if (nodes.size() > 1) {
                    for (int j = 1; j < nodes.size(); j++) {
                        if (nodes.get(j).hasClass("closedTopic")) {
                            isClosed = true;
                            break;
                        }
                    }
                }

                if (isClosed) {
                    continue;
                }

                posts.add(detail(nodes.get(0).attr("href")));
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return posts;
    }

    @Override
    public Post detail(String link) {
        return detail(link, false);
    }

    public Post detail(String link, boolean descInHtml) {
        try {
            Document doc = Jsoup.connect(link).get();
            Element vacancyTable = doc.selectFirst(".msgTable");
            Element msgHeader = vacancyTable.selectFirst(".messageHeader");
            Elements msgBodies = vacancyTable.select(".msgBody");
            String footer = vacancyTable.selectFirst(".msgFooter").ownText();

            return new Post(msgHeader.ownText(),
                    descInHtml ? msgBodies.get(1).html() : msgBodies.get(1).ownText(),
                    msgBodies.get(0).child(0).attr("href"),
                    parseDate(footer.substring(0, footer.indexOf(" ["))));
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    public static void main(String[] args) {
        LOG.info("Program started");
        SqlRuParse parser = new SqlRuParse();
        List<Post> vacancies = new ArrayList<>();
        for (int i = 1; i < MAX_NUM_PAGES; i++) {
            LOG.info("Parsing page " + i);
            vacancies.addAll(parser.list(BASE_URL + "/" + i));
        }
        vacancies.forEach(post -> LOG.debug("See below\n{}", post.toString()));
        LOG.info("Program finished");
    }
}