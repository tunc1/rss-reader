package app.service;

import java.util.List;
import java.util.LinkedList;
import java.util.Date;
import java.util.Comparator;
import java.util.Optional;
import java.text.SimpleDateFormat;
import app.dto.RSSFeed;
import org.springframework.stereotype.Component;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.w3c.dom.Document;  
import org.w3c.dom.NodeList;  
import org.w3c.dom.Node;  
import org.w3c.dom.Element;  
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.DocumentBuilder;  

@Component
public class RSSParser
{
	private final Logger logger=Logger.getLogger(RSSParser.class.getName());
	private static final SimpleDateFormat format=new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
	private static final Comparator<RSSFeed> comparator=(r1,r2)->r2.getPubDate().compareTo(r1.getPubDate());
	private static final DocumentBuilderFactory documentBuilderFactory=DocumentBuilderFactory.newInstance();
	
	public RSSParser()
	{
		System.setProperty("http.agent", "Mozilla/5.0");
	}
	public List<RSSFeed> get(Optional<String[]> urls)
	{
		List<RSSFeed> list=new LinkedList();
		if(urls.isPresent())
		{
			fillList(list,urls.get());
			list.sort(comparator);
		}
		return list;
	}
	private void fillList(List<RSSFeed> list,String[] urls)
	{
		Date now=new Date();
		List.of(urls).parallelStream().forEach(urlString->
			{
				try
				{
					DocumentBuilder documentBuilder=documentBuilderFactory.newDocumentBuilder();
					Document document=documentBuilder.parse(urlString);
					document.getDocumentElement().normalize();
					Element channel=(Element)document.getElementsByTagName("channel").item(0);
					String source=parseTag(channel,"title");
					NodeList nodeList=document.getElementsByTagName("item");
					for(int i=0;i<nodeList.getLength();i++)
					{
						Node node=nodeList.item(i);
						if(node.getNodeType()==Node.ELEMENT_NODE)
						{
							RSSFeed rssFeed=new RSSFeed();
							rssFeed.setSource(source);
							Element element=(Element)node;
							rssFeed.setTitle(parseTag(element,"title"));
							rssFeed.setLink(parseTag(element,"link"));
							String pubDate=parseTag(element,"pubDate");
							if(pubDate!=null&&pubDate.trim()!="")
								rssFeed.setPubDate(format.parse(pubDate));
							else
								rssFeed.setPubDate(now);
							rssFeed.setTimeDifference(timeDifference(rssFeed.getPubDate()));
							list.add(rssFeed);
						}
					}
				}
				catch(Exception e)
				{
					logger.log(Level.SEVERE,urlString,e);
				}
			}
		);
	}
	private String parseTag(Element element,String tag)
	{
		NodeList nodeList=element.getElementsByTagName(tag);
		if(nodeList.getLength()>0)
			return nodeList.item(0).getTextContent();
		return null;
	}
	private String timeDifference(Date pubDate)
	{
		Date now=new Date();
		long seconds=(now.getTime()-pubDate.getTime())/1000;
		if(seconds>86400*2)
			return (seconds/86400)+" days ago";
		if(seconds>=86400)
			return "1 day ago";
		if(seconds>=7200)
			return (seconds/3600)+" hours ago";
		if(seconds>3600)
			return "1 hour ago";
		if(seconds>=120)
			return (seconds/60)+" minutes ago";
		if(seconds>60)
			return "1 minute ago";
		return "Just Now";
	}
}