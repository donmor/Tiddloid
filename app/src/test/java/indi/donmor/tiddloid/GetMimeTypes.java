package indi.donmor.tiddloid;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.File;

public class GetMimeTypes {

	@Test
	public void run() {
		File file = new File("/usr/share/mime/packages/freedesktop.org.xml");
		try {
			Document document = Jsoup.parse(file,null);
			String[][] MIME_MapTableX = new String[1048576][2];
			String[][] MIME_MapAliasX = new String[1048576][2];
			int k = 0,w=0;
			for (int i = 0;i<document.getElementsByTag("mime-type").size();i++){
				Element mime_type = document.getElementsByTag("mime-type").get(i);
				Elements glob = mime_type.getElementsByTag("glob");
				String[] ext = new String[glob.size()];
				for (int j = 0;j<glob.size();j++){
					ext[j] = glob.get(j).attr("pattern");
				}
				for (String ex:ext){
					if (ex.charAt(0)=='*' &&ex.charAt(1)=='.'){
						MIME_MapTableX[k][0]=mime_type.attr("type");
						MIME_MapTableX[k][1]=ex;
					k++;}
				}
				Elements alias = mime_type.getElementsByTag("alias");
				String[] aliasArray = new String[alias.size()];
				for (int j = 0;j<alias.size();j++){
					aliasArray[j] = alias.get(j).attr("type");
				}
				for (String al:aliasArray){
						MIME_MapAliasX[w][0]=mime_type.attr("type");
						MIME_MapAliasX[w][1]=al;
					w++;
				}
			}
//			System.out.println(k);
//			String[][] MIME_MapTable = new String[k][2];
//			System.arraycopy(MIME_MapTableX,0,MIME_MapTable,0,k);
//			for (String[] i : MIME_MapTable) {
//				System.out.println(i[0]+"\t"+i[1]);
////				System.out.println(i[1]);
//			}
			System.out.println(w);
			String[][] MIME_MapAlias = new String[w][2];
			System.arraycopy(MIME_MapAliasX,0,MIME_MapAlias,0,w);
			for (String[] i : MIME_MapAlias) {
				System.out.println(i[0]+"\t"+i[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
