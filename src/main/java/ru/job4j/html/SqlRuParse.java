package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SqlRuParse {
    public static void main(String[] args) throws Exception {
        Document doc = Jsoup.connect("https://www.sql.ru/forum/job-offers").get();
        for (int i = 0; i < 5; i++) {
            Elements listPage = doc.select(".sort_options");
            Elements row = doc.select(".postslisttopic");
            for (Element td : row) {
                Element href = td.child(0);
                Element element = td.parent().child(5);
                System.out.println(href.attr("href"));
                System.out.println(href.text());
                System.out.println(element.text());
            }
            Elements row2 = listPage.select("a");
            Element hrefNextPage = row2.get(i);
            System.out.println("------------------------------------------------------");
            doc = Jsoup.connect(hrefNextPage.attr("href")).get();
        }
    }
}
