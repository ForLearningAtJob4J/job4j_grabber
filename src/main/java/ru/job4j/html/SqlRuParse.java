package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//import java.io.FileInputStream;
//import java.util.Properties;

public class SqlRuParse {
    private static void setProxy() {
        System.getProperties().put("http.proxyHost", "127.0.0.1");
        System.getProperties().put("http.proxyPort", "3128");
        System.getProperties().put("https.proxyHost", "127.0.0.1");
        System.getProperties().put("https.proxyPort", "3128");
    }

    public static void main(String[] args) throws Exception {
//        boolean useProxy = false;
//        try (FileInputStream in = new FileInputStream("rabbit.properties")) {
//            Properties config = new Properties();
//            config.load(in);
//            useProxy = Boolean.parseBoolean(config.getProperty("proxy.use"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.getProperties().forEach((o, o2) -> System.out.println(o + " " + o2));
        if ("sapunovsa".equals(System.getProperty("user.name"))) {
            setProxy();
        }

        Document doc = Jsoup.connect("https://www.sql.ru/forum/job-offers").get();
        Elements row = doc.select(".forumTable tr");
        for (int i = 1; i < row.size(); i++) {
            Element tr = row.get(i);
            System.out.format("%s%n%s [%s]%n", tr.child(1).child(0).attr("href"), tr.child(1).child(0).text(), tr.child(tr.children().size() - 1).text());
        }
    }
}