package ranker;

import java.util.HashMap;
import java.util.Set;

public class PageRanker {

    private HashMap<String, Double> PageRankScores;
    private HashMap<String, Set<String>> inLinks;

    private HashMap<String, Set<String>> outLinks;

    private static final double dampingFactor = 0.85;

    private static final double threshold = 0.0001;


    public PageRanker(HashMap<String, Set<String>> inLinks, HashMap<String, Set<String>> outLinks) {
        this.inLinks = inLinks;
        this.outLinks = outLinks;
        // Making sure that only crawled pages are considered
        for (String key : inLinks.keySet()) {
            if (!outLinks.containsKey(key))
                inLinks.remove(key);
        }
        PageRankScores = new HashMap<>();
        for (String key : inLinks.keySet())
            PageRankScores.put(key, 1.0 / inLinks.size());
    }

    public void CalculatePageRanks() {
        HashMap<String, Double> newPageRankScores = new HashMap<>();
        while (true) {
            double totalSum = 0;
            for (String page : inLinks.keySet()) {
                double sum = 0;
                for (String inlink : inLinks.get(page)) {
                    int outDegree = outLinks.get(inlink).size();
                    sum += PageRankScores.get(inlink) / outDegree;
                }
                double newPageRankScore = (1 - dampingFactor) / inLinks.size() + dampingFactor * sum;
                newPageRankScores.put(page, newPageRankScore);
                totalSum += newPageRankScore;
            }
                if(Converges(PageRankScores, newPageRankScores))
                {
                    PageRankScores=newPageRankScores;
                    this.Normalize(PageRankScores,totalSum);
                    break;
                }
                PageRankScores = newPageRankScores;
                this.Normalize(PageRankScores,totalSum);
            }
        }

    private boolean Converges(HashMap<String, Double> oldPageRankScores, HashMap<String, Double> newPageRankScores) {
        for (String page : oldPageRankScores.keySet()) {
            if (Math.abs(oldPageRankScores.get(page) - newPageRankScores.get(page)) > threshold)
                return false;
        }
        return true;
    }

    private void Normalize(HashMap<String, Double> PageRankScores,double totalSum){
        for (String page : PageRankScores.keySet())
            PageRankScores.put(page, PageRankScores.get(page) / totalSum);
    }

    public HashMap<String, Double> getPageRankScores() {
        return PageRankScores;
    }
}

