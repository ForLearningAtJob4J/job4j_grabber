package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;

public class SqlRuParse {
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
    private static final int NUM_PAGES = 5;

    private static void setProxy() {
        System.getProperties().put("http.proxyHost", "127.0.0.1");
        System.getProperties().put("http.proxyPort", "3128");
        System.getProperties().put("https.proxyHost", "127.0.0.1");
        System.getProperties().put("https.proxyPort", "3128");
    }

    private static LocalDateTime parseDate(String s) {
        String[] arr = s.split(", ");
        LocalDate ld;
        if ("сегодня".equals(arr[0])) {
            ld = LocalDate.now();
        } else if ("вчера".equals(arr[0])) {
            ld = LocalDate.now().minusDays(1);
        } else {
            String[] splittedDate = arr[0].split(" ");
            ld = LocalDate.of(2000 + Integer.parseInt(splittedDate[2]), MONTHS.get(splittedDate[1]), Integer.parseInt(splittedDate[0]));
        }
        return LocalDateTime.of(ld, LocalTime.parse(arr[1]));
    }

    private static void parsePage(int pageNum) throws IOException {
        Document doc = Jsoup.connect("https://www.sql.ru/forum/job-offers/" + pageNum).get();
        Elements row = doc.select(".forumTable tr");
        for (int i = 1; i < row.size(); i++) {
            Element tr = row.get(i);
            System.out.format("%s%n%s [%s]%n",
                    tr.child(1).child(0).attr("href"),
                    tr.child(1).child(0).text(),
                    parseDate(tr.child(tr.children().size() - 1).text()));
        }
    }

    public static void main(String[] args) throws Exception {
        if ("sapunovsa".equals(System.getProperty("user.name"))) {
            setProxy();
        }

        for (int i = 0; i < NUM_PAGES; i++) {
            parsePage(i);
        }
    }
}