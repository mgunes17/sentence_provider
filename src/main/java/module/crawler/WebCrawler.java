package module.crawler;

import content.UrlContent;
import module.processor.Processor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by mustafa on 23.04.2017.
 */
public class WebCrawler {
    private final static Logger logger = LoggerFactory.getLogger(WebCrawler.class);
    private Queue<String> unvisitedPageUrls;
    private Queue<WebPage> webPageQueue;
    private UrlContent urlContent;
    private Processor processor;
    private Set<String> visitedUrls;

    public WebCrawler(Processor processor){
        unvisitedPageUrls = new LinkedList<String>();
        webPageQueue = new LinkedList<WebPage>();
        urlContent = new UrlContent();
        this.processor = processor;
        visitedUrls = new HashSet<String>();
    }

    public void crawl(int count) {
        WebPage webPage;
        String url = unvisitedPageUrls.poll();
        int i = 0;

        while (i < count && url != null) { //count, toplam deneme sayısı? toplam başarılı deneme sayısı?
            try {
                webPage = new WebPage(url);
                webPage.setContent(urlContent.fetchContent(webPage.getUrl()));

                if (checkAcceptanceOfDocument(urlContent.getDocument())) {
                    webPageQueue.offer(webPage);
                    updateUnvisitedPageUrls();

                    logger.info("Succesfully connected to: " + url);
                    i++;
                }
                url = unvisitedPageUrls.poll();
            } catch (Exception ex) {
                logger.warn(ex.getMessage());
            }
        }

        System.out.println("Kuyrukta bekleyen url yok.");
        System.out.println("Veri indirme işlemi tamamlandı.");
        System.out.println("Veriler kayda hazırlanıyor.");
        processor.process(webPageQueue);
    }

    private boolean checkAcceptanceOfDocument(Document document) {
        Element htmlTag = document.select("html").first();
        String langAttribute = htmlTag.attr("lang");

        return langAttribute != null && (langAttribute.equals("tr-TR") || langAttribute.equals("tr"));
    }

    private void updateUnvisitedPageUrls(){
        List<String> linksOnPage = urlContent.extractLinks();
        for(String link: linksOnPage){
            if(!visitedUrls.contains(link)){
                visitedUrls.add(link);

                if(isAcceptable(link))
                    unvisitedPageUrls.add(link);
            }
        }
    }

    private boolean isAcceptable(String url) {
        return !url.contains("#") && !url.contains("action=edit") && !url.contains("action=history")
                && !url.contains("veaction=edit") && !url.contains(".jpg") && !url.contains(".png");
    }

    // getters and setters
    public void addUrl(String url) {
        unvisitedPageUrls.add(url);
    }

    public Queue<String> getUnvisitedPageUrls() {
        return unvisitedPageUrls;
    }
}
