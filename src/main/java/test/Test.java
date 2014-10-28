package test;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.seagatesoft.sde.DataRecord;
import org.seagatesoft.sde.DataRegion;
import org.seagatesoft.sde.TagTree;
import org.seagatesoft.sde.columnaligner.ColumnAligner;
import org.seagatesoft.sde.columnaligner.PartialTreeAligner;
import org.seagatesoft.sde.datarecordsfinder.DataRecordsFinder;
import org.seagatesoft.sde.datarecordsfinder.MiningDataRecords;
import org.seagatesoft.sde.dataregionsfinder.DataRegionsFinder;
import org.seagatesoft.sde.dataregionsfinder.MiningDataRegions;
import org.seagatesoft.sde.tagtreebuilder.DOMParserTagTreeBuilder;
import org.seagatesoft.sde.tagtreebuilder.TagTreeBuilder;
import org.seagatesoft.sde.treematcher.SimpleTreeMatching;
import org.seagatesoft.sde.treematcher.TreeMatcher;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class Test {

	public static void main(String[] args) throws IOException, SAXException {
		
		String doc= "<html><head>test</head><body>test</body></html>";
		System.out.println(doc);
		InputSource inputSource = new InputSource( new StringReader(doc) );	
		TagTreeBuilder builder = new DOMParserTagTreeBuilder();
		
		TagTree tagTree = builder.buildTagTree(doc,"http://en.wikipedia.org", true);
		
		TreeMatcher matcher = new SimpleTreeMatching();
		DataRegionsFinder dataRegionsFinder = new MiningDataRegions( matcher );
		List<DataRegion> dataRegions = dataRegionsFinder.findDataRegions(tagTree.getRoot(), 9, 0.6);
		DataRecordsFinder dataRecordsFinder = new MiningDataRecords( matcher );
		DataRecord[][] dataRecords = new DataRecord[ dataRegions.size() ][];
		
		for( int dataRecordArrayCounter = 0; dataRecordArrayCounter < dataRegions.size(); dataRecordArrayCounter++)	{
			DataRegion dataRegion = dataRegions.get( dataRecordArrayCounter );
			dataRecords[ dataRecordArrayCounter ] = dataRecordsFinder.findDataRecords(dataRegion, 0.6);
		}
		
		ColumnAligner aligner = new PartialTreeAligner( matcher );				
		List<String[][]> dataTables = new ArrayList<String[][]>();

		for(int tableCounter=0; tableCounter< dataRecords.length; tableCounter++)	{
			
			String[][] dataTable = aligner.alignDataRecords( dataRecords[tableCounter] );
			if ( dataTable != null )	{
				dataTables.add( dataTable );
			}
		}

		for ( String[][] table: dataTables)	{
			
			for (String[] row: table)	{
				
				List<String> record = new ArrayList<String>();
				for(String item: row) {				
					String itemText = item;
					if (itemText == null) {
						itemText = "";
					}
					
					else if(itemText.contains("</a>")) {
						itemText = itemText.replaceAll("<a(.*?)</a>", "");
					}
					else if(itemText.contains("<img")) {
						itemText = itemText.replaceAll("<img(.*?)/>", "");
					}
					
					itemText = itemText.trim();
					if(!itemText.equals("") && itemText != null && !record.contains(itemText)) {
						//itemText = "|+|" + itemText + "|+|";
						record.add(itemText);
						
					}
				}
				System.out.println(record);
				System.out.println("---------------");
			}			
		}
		

	}

	public static String getHtml(String urlString) {
		try {
			StringBuffer html = new StringBuffer();
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)");
			conn.setRequestMethod("GET");
			InputStreamReader isr = new InputStreamReader(conn.getInputStream());
			BufferedReader br = new BufferedReader(isr);
			String temp;
			while ((temp = br.readLine()) != null) {
				html.append(temp).append("\n");
			}
			br.close();
			isr.close();
			return html.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
