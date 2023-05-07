package stopwordsrm;

public class StopWordsTest {

    public static void main(String[] args)
    {
        StopWordsRemover remover = new StopWordsRemover("StopWords.txt");
        String text="webpage1 STARTING . . . There are lots of ways to create web pages using already coded programmes. These lessons will teach you how to use the underlying HyperText Markup Language - HTML. HTML isn't computer code, but is a language that uses US English to enable texts (words, images, sounds) to be inserted and formatting such as colo(u)r and centre/ering to be written in. The process is fairly simple; the main difficulties often lie in small mistakes - if you slip up while word processing your reader may pick up your typos, but the page will still be legible. However, if your HTML is inaccurate the page may not appear - writing web pages is, at the least, very good practice for proof reading! Learning HTML will enable you to: create your own simple pages read and appreciate pages created by others develop an understanding of the creative and literary implications of web-texts have the confidence to branch out into more complex web design A HTML web page is made up of tags. Tags are placed in brackets like this < tag > . A tag tells the browser how to display information. Most tags need to be opened < tag > and closed < /tag >. To make a simple web page you need to know only four tags: < HTML > tells the browser your page is written in HTML format < HEAD > this is a kind of preface of vital information that doesn't appear on the screen. < TITLE >Write the title of the web page here - this is the information that viewers see on the upper bar of their screen. (I've given this page the title 'webpage1'). < BODY >This is where you put the content of your page, the words and pictures that people read on the screen. All these tags need to be closed. EXERCISE Write a simple web page. Copy out exactly the HTML below, using a WP program such as Notepad. Information in italics indicates where you can insert your own text, other information is HTML and needs to be exact. However, make sure there are no spaces between the tag brackets and the text inside. (Find Notepad by going to the START menu\\ PROGRAMS\\ ACCESSORIES\\ NOTEPAD). < HTML > < HEAD > < TITLE > title of page< /TITLE > < /HEAD > < BODY> write what you like here: 'my first web page', or a piece about what you are reading, or a few thoughts on the course, or copy out a few words from a book or cornflake packet. Just type in your words using no extras such as bold, or italics, as these have special HTML tags, although you may use upper and lower case letters and single spaces. < /BODY > < /HTML > Save the file as 'first.html' (ie. call the file anything at all) It's useful if you start a folder - just as you would for word-processing - and call it something like WEBPAGES, and put your first.html file in the folder. NOW - open your browser. On Netscape the process is: Top menu; FILE\\ OPEN PAGE\\ CHOOSE FILE Click on your WEBPAGES folder\\ FIRST file Click 'open' and your page should appear. On Internet Explorer: Top menu; FILE\\ OPEN\\ BROWSE Click on your WEBPAGES folder\\ FIRST file Click 'open' and your page should appear. If the page doesn't open, go back over your notepad typing and make sure that all the HTML tags are correct. Check there are no spaces between tags and internal text; check that all tags are closed; check that you haven't written < HTLM > or < BDDY >. Your page will work eventually. Make another page. Call it somethingdifferent.html and place it in the same WEBPAGES folder as detailed above. start formatting in lesson two back to wws index\r\n";
        text=remover.RemoveStopWords(text);
        System.out.println(text);
        String text2="<html><head><title>Example </title></head><body><p>This is an example.</p></body></html>";
        text2=DocumentCleaner.RemoveHtmlTags(text2);
        text2=DocumentCleaner.RemoveSpecialCharacters(text2);
        System.out.println(text2);
    }

}
