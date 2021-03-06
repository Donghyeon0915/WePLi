/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Crawler;

import Dto.Song.SongDto;
import java.io.IOException;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author joon
 */

public abstract class Crawler {
    protected String URL;

    public ArrayList<Element> getChartBody(String url){
        ArrayList<Element> crawl_Result = new ArrayList<>();  
        Document doc = null;
              
        try { doc = (Document) Jsoup.connect(url).get(); }
        catch (IOException e){ e.printStackTrace(); }
        
       Element element = null;
       
        // 검색 결과가 없는 경우
        if(doc.select("tbody").size()==0) element = null;
        else{
            if(doc.select("tbody").size()==3) //bugs검색에서 bugs는 tbody size가 3
                element = doc.select("tbody").get(1);
            else                             // bugs 인기차트는 size=2 / 나머지 1
                element = doc.select("tbody").get(0);
            
            for(Element el : element.select("tr")) crawl_Result.add(el);
        }

        return crawl_Result;
    }
    // 크롤링 결과를 반환하는 메소드
    public ArrayList<SongDto> getSongList(String url){  
        ArrayList<Element> chartBody = this.getChartBody(url);
        
        return this.parseSongChart(chartBody); // 노래 리스트 리턴됨 
    }
    public String getURL(){ return this.URL; }
    
    // 음악 사이트의 차트 테이블에서 곡 정보를 파싱하는 메소드
    abstract protected ArrayList<SongDto> parseSongChart(ArrayList<Element> chartBody);
}
