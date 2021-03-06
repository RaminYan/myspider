package com.sutao.myspider.crawl.movie;

import com.sutao.myspider.crawl.AbstractCrawl;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

public class MovieCrawl extends AbstractCrawl<MovieCrawl.Item> {
    private String seperator = "/";
    private String url = "http://s.dydytt.net";
    private String search = url + seperator + "plus/s0.php?keyword=2019&searchtype=titlekeyword&channeltype=0&orderby=&kwtype=0&pagesize=10&typeid=1&PageNo=";
    private static Comparator<MovieCrawl.Item> comparator = (MovieCrawl.Item o1, MovieCrawl.Item o2) -> Float.valueOf(o2.getMark()).compareTo(Float.valueOf(o1.getMark()));
    protected class Item {
        public String text;
        public String link;
        public String mark;
        public String download;

        public String getDownload() {
            return download;
        }

        public void setDownload(String download) {
            this.download = download;
        }

        public String getMark() {
            return mark;
        }

        public void setMark(String mark) {
            this.mark = mark;
        }

        public Item(String text, String link) {
            this.text = text;
            this.link = link;
            this.download = "";
            this.mark = "1.0";
        }

        public String getText() {
            return text;
        }

        public String getLink() {
            return link;
        }

        @Override
        public String toString() {
            return "CurrentThread=" + Thread.currentThread().getId() +
                    "JobEntity{" +
                    "text='" + text + '\'' +
                    ", link='" + link + '\'' +
                    ", mark='" + mark + '\'' +
                    ", download='" + download + '\'' +
                    '}';
        }
    }

    public MovieCrawl() {
        setComparator(comparator);
    }

    public ArrayList<Item> getMovieList(int page) {
        Document doc = null;
        ArrayList list = new ArrayList<Item>();

        try {
            doc = Jsoup.connect(search + "" + page).get();
            Elements moveList = doc.select("div.co_content8").first().select("ul").select("table");
            for (Element e : moveList) {
                Element href = e.select("tr").first().select("a").first();
                String link = href.attr("href");

                Item item = new Item(href.text(), url + link);
                list.add(item);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }


    public ArrayList<Item> getMovieDownloadUrl(ArrayList<Item> movieList) {
        ArrayList<Item> downloadList = new ArrayList<>();
        for (Item item : movieList) {
            try {
                Thread.sleep(3000);
                Document doc = Jsoup.connect(item.getLink()).get();
                Element href = doc.select("div#Zoom").first().select("table").first().select("tr").first().select("a").first();
                //download url
                String link = href.text();
                if (link.endsWith(".exe") || link.endsWith(".rar") || link.endsWith("html")) continue;
                item.setDownload(link);
                //get movie mark grade
                Element e = doc.select("div#read_tpc").first();
                if (null == e) e = doc.select("div#Zoom").select("p").first();
                String[] content = e.text().split("◎");
                String mark = "1.0";
                int skip = "评分".length();
                for (String line : content) {
                    int index = line.indexOf("评分");
                    int i = index + skip;
                    int start = -1;
                    if (index != -1) {
                        for (; i < line.length(); i++) {
                            if (-1 == start && (line.charAt(i) <= '9' && line.charAt(i) >= '0')) {
                                start = i;
                            } else if (line.charAt(i) == '/') {
                                break;
                            }
                            ;
                        }
                        mark = line.substring(start, i).trim();
                        try {
                            Float n = Float.valueOf(mark);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                            mark = "1.0";
                        }

                        break;
                    }
                }
                item.setMark(mark);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (item.getDownload().isEmpty()) continue;
                System.out.println(item.toString());
                downloadList.add(item);
            }
        }

        return downloadList;
    }

    @Override
    public ArrayList<Item> run(int page) {
        return getMovieDownloadUrl(getMovieList(page));
    }

    @Override
    public void toHtmlImpl(ArrayList<Item> total, StringBuilder bw) {
        for (Item item : total) {
            bw.append("      <tr>\n");
            bw.append("        <td><a href=" + item.getDownload() + ">" + item.getText() + "</a></td>\n");
            bw.append("        <td>" + item.getMark() + "</td>\n");
            bw.append("      </tr>\n");
        }
    }

}


