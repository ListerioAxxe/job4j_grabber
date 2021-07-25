package ru.job4j.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.Parse;
import ru.job4j.grabber.Post;
import ru.job4j.grabber.SqlRuDateTimeParser;

import javax.print.Doc;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlRuParse implements Parse {
    private static SqlRuDateTimeParser dateParser;
    private static final Pattern DATE_PATTERN = Pattern.compile(".*, \\d{2}:\\d{2}");

    public SqlRuParse(SqlRuDateTimeParser dateParser) {
        this.dateParser = dateParser;
    }

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
        SqlRuDateTimeParser sq = new SqlRuDateTimeParser();
        SqlRuParse sqlRuParse = new SqlRuParse(sq);
        String link = "https://www.sql.ru/forum/1337535/vakansiya-administrator-subd-ms-sql";
        System.out.println(sqlRuParse.detail(link));
        List<Post> posts = sqlRuParse.list("https://www.sql.ru/forum/job-offers");
        System.out.println(posts.get(3));
    }

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> posts = new ArrayList<>();
        Document doc = Jsoup.connect(link).get();
        Elements row = doc.select(".postslisttopic");
        for (int i = 0; i < row.size(); i++) {
            Element href = row.get(i).child(0);
            posts.add(detail(href.attr("href")));
        }
        return posts;
    }

    @Override
    public Post detail(String link) throws IOException {
        Document doc = Jsoup.connect(link).get();
        Element msgBody = doc.select(".msgBody").get(1);
        Element msgFooter = doc.selectFirst(".msgFooter");
        Matcher footerMatcher = DATE_PATTERN.matcher(msgFooter.text());
        if (!footerMatcher.find()) {
            throw new IllegalStateException("Невозможно разобрать данные поста");
        }
        Element msgHeader = doc.selectFirst(".messageHeader");
        String title = msgHeader.text().replace("[new]", "").trim();
        return new Post(
                link,
                title,
                dateParser.parse(footerMatcher.group()),
                msgBody.text()
        );
    }
}
