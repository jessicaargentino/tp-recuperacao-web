import java.util.List;
import java.io.File;
import java.io.FileWriter;

import com.opencsv.CSVWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import java.util.Set;
import java.lang.Integer;


import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import model.FetchRowModel;
import model.StatModel;
import model.VisitRowModel;
import service.crawler.CrawlerService;

public class App {
    public static void main(String[] args) throws Exception {

        String rootFolder = "/tmp/crawl";
        int numberOfCrawlers = 4;

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(rootFolder);
        config.setMaxPagesToFetch(20000);
        config.setMaxDepthOfCrawling(16);
        config.setPolitenessDelay(100);
        config.setIncludeBinaryContentInCrawling(true);
        config.setResumableCrawling(false);
        config.setIncludeHttpsPages(true);


        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        controller.addSeed("https://www.bbc.com/portuguese");
        controller.start(CrawlerService.class, numberOfCrawlers);

        List<Object> crawlersLocalData = controller.getCrawlersLocalData();

        File fileVisit = new File("/tmp/data_visit.csv");
        File fileFetch = new File("/tmp/data_fetch.csv");

        FileWriter o1 = new FileWriter(fileVisit);
        FileWriter o2 = new FileWriter(fileFetch);

        CSVWriter writer1 = new CSVWriter(o1);
        CSVWriter writer2 = new CSVWriter(o2);

        List<String[]> data1 = new ArrayList<>();
        List<String[]> data2 = new ArrayList<>();

        data1.add(new String[]{"url", "size (byte)", "#outlink", "content type"});
        data2.add(new String[]{"url", "status"});

        HashMap<String, VisitRowModel> gVisitedTable = new HashMap<>();
        HashMap<String, FetchRowModel> gFetchTable = new HashMap<>();

        int totalF = 0;
        int sucF = 0;
        int failF = 0;
        long grantF = 0;

        Set<String> uniqueIn = new HashSet<>();
        Set<String> uniqueOut = new HashSet<>();

        for (Object localData : crawlersLocalData) {
            StatModel stat = (StatModel) localData;
            Set<String> setOfVKeys = stat.getVisit().keySet();

            for (String key : setOfVKeys) {
                gVisitedTable.put(key, stat.getVisit().get(key));
            }

            Set<String> setOfFKeys = stat.getFetch().keySet();
            for (String key : setOfFKeys) {
                gFetchTable.put(key, stat.getFetch().get(key));
            }

            Set<String> setOfSKeys = stat.getSeen().keySet();
            for (String key : setOfSKeys) {
                if (stat.getSeen().get(key)) {
                    // in-net
                    uniqueIn.add(key);
                } else {
                    // out-net
                    uniqueOut.add(key);
                }
            }

            totalF += stat.getRealFetch();
            sucF += stat.getSucFetch();
            failF += stat.getFailFetch();
            grantF += stat.getOutLink();
        }


        int one = 0;
        int two = 0;
        int three = 0;
        int four = 0;
        int five = 0;

        HashMap<String, Integer> typeTable = new HashMap<>();

        Set<String> setOfVKeys = gVisitedTable.keySet();
        for (String key : setOfVKeys) {
            long sz = gVisitedTable.get(key).sizeByte;
            String tp = gVisitedTable.get(key).contentType;
            data1.add(new String[]{gVisitedTable.get(key).url,
                    String.valueOf(gVisitedTable.get(key).sizeByte),
                    String.valueOf(gVisitedTable.get(key).outLink),
                    gVisitedTable.get(key).contentType});

            if (!typeTable.containsKey(tp)) {
                typeTable.put(tp, 0);
            }
            typeTable.put(tp, typeTable.get(tp) + 1);

            if (sz < (long) 1024) {
                one++;
            } else if (sz < (long) 10240) {
                two++;
            } else if (sz < (long) 102400) {
                three++;
            } else if (sz < (long) 1048576) {
                four++;
            } else {
                five++;
            }
        }

        HashMap<Integer, Integer> statusTable = new HashMap<>();

        Set<String> setOfFKeys = gFetchTable.keySet();
        for (String key : setOfFKeys) {
            Integer st = gFetchTable.get(key).status;
            data2.add(new String[]{gFetchTable.get(key).url, String.valueOf(gFetchTable.get(key).status)});

            if (!statusTable.containsKey(st)) {
                statusTable.put(st, 0);
            }
            statusTable.put(st, statusTable.get(st) + 1);

        }

        writer1.writeAll(data1);
        writer2.writeAll(data2);
        writer1.close();
        writer2.close();

        FileWriter myWriter = new FileWriter("/tmp/data_report.txt");
        myWriter.write("Relatório de dados\n");
        myWriter.write("Número de threads: " + (numberOfCrawlers) + "\n\n");

        myWriter.write("Fetch Statistics:\n");
        myWriter.write("================\n");
        myWriter.write("# fetches attempted: " + (totalF) + "\n");
        myWriter.write("# fetches succeeded: " + (sucF) + "\n");
        myWriter.write("# fetches failed or aborted: " + (failF) + "\n\n");

        myWriter.write("Outgoing URLs:\n");
        myWriter.write("================\n");
        myWriter.write("Total URLs extracted: " + (grantF) + "\n");
        myWriter.write("# unique URLs extracted: " + (uniqueIn.size() + uniqueOut.size()) + "\n");
        myWriter.write("# unique URLs within News Site: " + (uniqueIn.size()) + "\n");
        myWriter.write("# unique URLs outside News Site: " + (uniqueOut.size()) + "\n\n");

        myWriter.write("Status Codes:\n");
        myWriter.write("================\n");
        Set<Integer> setOfStatusKeys = statusTable.keySet();
        for (Integer each : setOfStatusKeys) {
            myWriter.write((each) + ": " + (statusTable.get(each)) + "\n");
        }

        myWriter.write("\nFile Sizes:\n");
        myWriter.write("================\n");
        myWriter.write("< 1KB: " + one + "\n");
        myWriter.write("1KB ~ <10KB: " + two + "\n");
        myWriter.write("10KB ~ <100KB: " + three + "\n");
        myWriter.write("100KB ~ <1MB: " + four + "\n");
        myWriter.write(">= 1MB: " + five + "\n\n");


        myWriter.write("Content Types:\n");
        myWriter.write("================\n");
        Set<String> setOfTypeKeys = typeTable.keySet();
        for (String each : setOfTypeKeys) {
            myWriter.write(each + ": " + typeTable.get(each) + "\n");
        }
        myWriter.close();
    }
}