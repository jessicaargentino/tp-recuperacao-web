package model;

import java.util.HashMap;

public class StatModel {
    private final HashMap<String, VisitRowModel> visitedTable = new HashMap<>();
    private final HashMap<String, FetchRowModel> fetchTable = new HashMap<>();
    private final HashMap<String, Boolean> seenTable = new HashMap<>();
    private long outLink = 0;
    private int realFetch = 0;
    private int sucFetch = 0;
    private int failFetch = 0;

    public long getOutLink() {
        return this.outLink;
    }

    public void incOutLink(long incNum) {
        this.outLink = this.outLink + incNum;
    }


    public int getRealFetch() {
        return this.realFetch;
    }

    public void incRealFetch() {
        this.realFetch++;
    }

    public void decRealFetch() {
        this.realFetch--;
    }


    public int getSucFetch() {
        return this.sucFetch;
    }

    public void incSucFetch() {
        this.sucFetch++;
    }

    public int getFailFetch() {
        return this.failFetch;
    }

    public void incFailFetch() {
        this.failFetch++;
    }

    public void decFailFetch() {
        this.failFetch--;
    }


    public void addVisit(VisitRowModel row) {
        this.visitedTable.put(row.url, row);
    }

    public HashMap<String, VisitRowModel> getVisit() {
        return this.visitedTable;
    }

    public void addFetch(FetchRowModel row) {
        this.fetchTable.put(row.url, row);
    }

    public HashMap<String, FetchRowModel> getFetch() {
        return this.fetchTable;
    }

    public void deleteFetch(String key) {
        this.fetchTable.remove(key);
    }

    public void addSeen(String url) {
        String tep = url.toLowerCase();
        this.seenTable.put(url, tep.startsWith("https://www.bbc.com/portuguese"));
    }

    public HashMap<String, Boolean> getSeen() {
        return this.seenTable;
    }
}
