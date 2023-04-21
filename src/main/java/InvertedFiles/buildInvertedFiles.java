package InvertedFiles;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.*;
import indexer.*;
import structures.pair;

class buildInvertedFiles {



    public static   Map<String,Integer> lexicon=new HashMap<String,Integer>();
    //public static  Map<String,Integer> docsId=new HashMap<String,Integer>();
    public static   Map<Integer,List<pair<Integer,Integer>>> postings=new HashMap<Integer,List<pair<Integer,Integer>>>();
    public static void main(String[] args) {
        // TODO Auto-generated method stub

        List<Document> docSet=new ArrayList<Document>();
//		long end=0;
        //docSet.add()
        try {
            Document s=Jsoup.connect("https://www.york.ac.uk/teaching/cws/wws/webpage1.html").get();
            docSet.add(s);
//			docSet.add(Jsoup.connect("https://www.geeksforgeeks.org/set-in-java/").get());
//			docSet.add(Jsoup.connect("https://openjfx.io/openjfx-docs/#introduction").get());
//			docSet.add(Jsoup.connect("https://www.geeksforgeeks.org/using-underscore-in-numeric-literals-in-java/?ref=lbp").get());
//			docSet.add(Jsoup.connect("https://library.umbc.edu/").get());
//			docSet.add(Jsoup.connect("https://ur.umbc.edu/").get());
//			docSet.add(Jsoup.connect("https://gradschool.umbc.edu/discover/research/").get());
//			docSet.add(Jsoup.connect("https://research.umbc.edu/for-faculty/").get());
//			docSet.add(Jsoup.connect("https://bwtech.umbc.edu").get());
//			docSet.add(Jsoup.connect("https://umbc.edu/learn-more/get-going/?utm_source=UMBCedu&utm_medium=PreNav&utm_campaign=RFI-Nav#").get());
//			docSet.add(Jsoup.connect("https://umbc.edu/life-on-campus/").get());
//			docSet.add(Jsoup.connect("https://umbc.edu/life-on-campus/arts-and-culture/").get());
//			docSet.add(Jsoup.connect("https://umbc.edu/life-on-campus/athletics-recreation/").get());
//			docSet.add(Jsoup.connect("https://events.umbc.edu/").get());
//			docSet.add(Jsoup.connect("https://umbc.edu/life-on-campus/clubs-activities/").get());
//			docSet.add(Jsoup.connect("https://umbc.edu/life-on-campus/health-wellbeing/").get());
//			docSet.add(Jsoup.connect("https://reslife.umbc.edu").get());
//			docSet.add(Jsoup.connect("https://umbc.edu/learn-more/get-going/?utm_source=UMBCedu&utm_medium=PreNav&utm_campaign=RFI-Nav#").get());
//			docSet.add(Jsoup.connect("https://securelb.imodules.com/s/1325/lg20/form.aspx?sid=1325&gid=1&pgid=2240&cid=4286&bledit=1&appealcode=OIA003").get());
//			docSet.add(Jsoup.connect("https://umbc.edu/giving").get());
//			docSet.add(Jsoup.connect("https://gritstarter.umbc.edu").get());
//			docSet.add(Jsoup.connect("https://www.alumni.umbc.edu/").get());

//


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int docId=0;
        long f=System.currentTimeMillis();
        for(Document D:docSet)
        {
//			long start=System.currentTimeMillis();

            List<pair<String,String>> tokens=indexer.Normalize(D);
            List<Integer> tokensIds=convertTokensToIds(tokens);
            Map<Integer,Integer> WordsCounts=wordCounts(tokensIds);
            addToPostings(docId,WordsCounts);
            docId++;
//			end=System.currentTimeMillis();
//			System.out.println("finished : "+(end-start));
        }
        int i=0;

        for(Map.Entry<String,Integer> word: lexicon.entrySet())
        {
            System.out.print(i+"."+word.getKey()+" : ");

            List<pair<Integer,Integer>> postingsList=postings.get(word.getValue());

            for(pair<Integer,Integer> p:postingsList)
            {
                System.out.print("{docID : "+p.first+" tf : "+p.second+"}" );
            }
            System.out.println();
            i++;
        }

        long s=System.currentTimeMillis();
        System.out.println("total : "+(s-f));
        System.out.println("no of words : "+lexicon.size());
        System.out.println("no of postingsList : "+postings.size());
        System.out.println("no of docs : "+docSet.size());

//		 i=0;
//		for(Map.Entry<String,Integer> word: lexicon.entrySet())
//		{
//			System.out.println(i+"."+word.getKey()+" ");
//			i++;
//		}
//		int id=lexicon.get("mobile");
//
//		List<pair> postingList=postings.get(id);
//		for(pair e: postingList)
//		{
//			System.out.print("{docID : "+e.first+" tf : "+e.second+"}" );
//		}
//
    }

    public static List<Integer>  convertTokensToIds(List<pair<String,String>> tokens)
    {
        List<Integer> tokensIDs=new ArrayList<Integer>();
        for( pair<String,String> token:tokens)
        {
            if(lexicon.containsKey(token.first))
            {
                tokensIDs.add(lexicon.get(token.first));
            }
            else
            {
                int id=lexicon.size();
                lexicon.put(token.first, id);
                tokensIDs.add(id);
            }
        }
        return tokensIDs;
    }
    public static Map<Integer,Integer> wordCounts(List<Integer> tokenIds)
    {
        Map<Integer,Integer> wordCounts=new HashMap<Integer,Integer>();

        for(Integer id:tokenIds)
        {
            if(wordCounts.containsKey(id))
            {
                int newCount=wordCounts.get(id)+1;
                wordCounts.put(id, newCount);
            }
            else
            {
                wordCounts.put(id, 1);
            }
        }


        return wordCounts;
    }

    public static void addToPostings(int docID,Map<Integer,Integer> wordsCounts)
    {
        for(Map.Entry<Integer,Integer> record: wordsCounts.entrySet())
            if(postings.containsKey(record.getKey()))
            {
                List<pair<Integer,Integer>> postinglist=postings.get(record.getKey());
                pair<Integer,Integer> newposting=new pair<Integer,Integer>(docID,record.getValue());
                postinglist.add(newposting);
                //postings.put(record.getKey(),postinglist);
            }
            else
            {
                List<pair<Integer,Integer>> postingList=new ArrayList<>();
                pair<Integer,Integer> newposting=new pair<Integer,Integer>(docID,record.getValue());
                postingList.add(newposting);
                postings.put(record.getKey(), postingList);
            }
    }


}

