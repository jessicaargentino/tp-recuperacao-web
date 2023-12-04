package service.crawler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.parser.BinaryParseData;
import model.CheckTypeModel;
import model.FetchRowModel;
import model.StatModel;
import model.VisitRowModel;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import service.file.FileWriterService;

public class CrawlerService extends WebCrawler {

    private StatModel myStat;
    private static final Pattern EXTENSIONS = Pattern.compile(".*(\\.(css|js|mid|mp2|mp3|mp4|wav|avi" +
            "|mov|mpeg|ram|m4v|rm|smil|wmv|swf|wma|zip|rar|gz|json|jpg|jpeg|svg))$");

    public static CheckTypeModel checkValid(String contentType) {
        String tp = contentType.toLowerCase();
        List<String> accepted = new ArrayList<>(Arrays.asList("text/html"));
        for (String e : accepted) {
            if (tp.contains(e)) {
                return new CheckTypeModel(true, e);
            }
        }
        return new CheckTypeModel(false, "");
    }

    public CrawlerService() {
        myStat = new StatModel();
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        if (EXTENSIONS.matcher(href).matches()) {
            return false;
        }

        return true;
    }

    @Override
    public void onRedirectedStatusCode(Page page) {
        this.myStat.incRealFetch();
        FetchRowModel row2 = new FetchRowModel();
        row2.url = page.getWebURL().getURL();
        row2.status = page.getStatusCode();
        this.myStat.addFetch(row2);
        this.myStat.incFailFetch();
    }

    @Override
    public void onUnexpectedStatusCode(String urlStr, int statusCode, String contentType, String description) {
        if (checkValid(contentType).pass) {
            this.myStat.incRealFetch();
            FetchRowModel row2 = new FetchRowModel();
            row2.url = urlStr;
            row2.status = statusCode;
            this.myStat.addFetch(row2);
            this.myStat.incFailFetch();
        }
    }

    @Override
    public void onContentFetchError(Page page) {
        if (checkValid(page.getContentType()).pass) {
            this.myStat.incRealFetch();
            FetchRowModel row2 = new FetchRowModel();
            row2.url = page.getWebURL().getURL();
            row2.status = page.getStatusCode();
            this.myStat.addFetch(row2);
            this.myStat.incFailFetch();
        }
    }

    @Override
    public void visit(Page page) {
        if (!checkValid(page.getContentType()).pass) {
            return;
        }

        String ct = checkValid(page.getContentType()).cType;

        if (page.getParseData() instanceof HtmlParseData) {
            this.myStat.incRealFetch();
            FetchRowModel row2 = new FetchRowModel();
            row2.url = page.getWebURL().getURL();
            row2.status = page.getStatusCode();
            this.myStat.addFetch(row2);

            this.myStat.incSucFetch();
            HtmlParseData parseData = (HtmlParseData) page.getParseData();

            String html = parseData.getHtml();
            Document doc = Jsoup.parse(html);
            Elements elements = doc.select("p, h1, h2, h3, h4, h5, h6, li, ul, ol, a, table, div");
            StringBuilder textContent = new StringBuilder();

            for (Element element : elements) {
                textContent.append(element.text()).append("\n");
            }

            FileWriterService.writeFile(textContent.toString()
                    .replaceAll("[^\\p{L}\\p{N}\\s]", ""));

            Set<WebURL> links = parseData.getOutgoingUrls();

            for (WebURL i : links) {
                this.myStat.addSeen(i.getURL());
            }

            this.myStat.incOutLink(links.size());
            VisitRowModel row1 = new VisitRowModel();
            row1.url = page.getWebURL().getURL();
            row1.sizeByte = parseData.getHtml().length();
            row1.outLink = links.size();
            row1.contentType = ct;
            this.myStat.addVisit(row1);

        } else if (page.getParseData() instanceof BinaryParseData) {
            this.myStat.incRealFetch();
            FetchRowModel row2 = new FetchRowModel();
            row2.url = page.getWebURL().getURL();
            row2.status = page.getStatusCode();
            this.myStat.addFetch(row2);

            this.myStat.incSucFetch();
            VisitRowModel row1 = new VisitRowModel();
            row1.url = page.getWebURL().getURL();
            row1.sizeByte = page.getContentData().length;
            row1.outLink = 0;
            row1.contentType = ct;
            this.myStat.addVisit(row1);
        } else {
            System.exit(0);
        }
    }

    @Override
    public Object getMyLocalData() {
        return myStat;
    }
}