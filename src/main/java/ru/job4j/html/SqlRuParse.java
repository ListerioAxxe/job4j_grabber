package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.Post;
import ru.job4j.grabber.SqlRuDateTimeParser;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlRuParse {
    private static SqlRuDateTimeParser dateParser = new SqlRuDateTimeParser();
    private static final Pattern DATE_PATTERN = Pattern.compile(".*, \\d{2}:\\d{2}");

    public static Post parsePost(Element href) throws IOException {
        Document doc = Jsoup.connect(href.attr("href")).get();
        Element msgBody = doc.select(".msgBody").get(1);
        Element msgFooter = doc.selectFirst(".msgFooter");
        Matcher footerMatcher = DATE_PATTERN.matcher(msgFooter.text());
        if (!footerMatcher.find()) {
            throw new IllegalStateException("Невозможно разобрать дату поста");
        }
        return new Post(
                href.attr("href"),
                href.text(),
                dateParser.parse(footerMatcher.group()),
                msgBody.text()
        );
    }

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
