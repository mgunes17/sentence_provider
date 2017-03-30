package momo.content;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ercan on 23.03.2017.
 */
public class UrlContent implements Content{
    private Document document;

    public String fetchContent(String sourceName) throws IOException {
        try {
            document = Jsoup.connect(sourceName).get();
            return extractBodyTextFromHtml();
        } catch (IOException e) {
            throw  new IOException();
        }
    }

    private String extractBodyTextFromHtml(){
        return document.body().text();
    }

    public List<String> extractLinks(){
        List<String> linksInHtml = new ArrayList<String>();
        Elements links = document.select("a[href]");

        for (Element link : links) {
            linksInHtml.add(link.attr("abs:href"));
        }

        return linksInHtml;
    }

}
