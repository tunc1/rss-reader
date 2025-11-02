package app.service;

import java.util.List;
import java.util.LinkedList;
import java.util.Date;
import java.util.Comparator;
import java.util.Optional;
import java.util.Locale;
import java.text.SimpleDateFormat;
import app.dto.RSSFeed;
import app.util.TimeUtil;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.w3c.dom.Document;  
import org.w3c.dom.NodeList;  
import org.w3c.dom.Node;  
import org.w3c.dom.Element;  
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.DocumentBuilder;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.net.URL;
import java.net.URLConnection;

@Component
public class RSSParser
{
	private final Logger logger=Logger.getLogger(RSSParser.class.getName());
	private static final SimpleDateFormat format=new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
	private static final Comparator<RSSFeed> comparator=(r1,r2)->r2.getPubDate().compareTo(r1.getPubDate());
	private static final DocumentBuilderFactory documentBuilderFactory=DocumentBuilderFactory.newInstance();
	private final ExecutorService executorService;
	private final TimeUtil timeUtil;
	private final int timeout;
	
	public RSSParser(@Value("${pool-size}") int poolSize,@Value("${timeout}") int timeout,TimeUtil timeUtil)
	{
		executorService=Executors.newFixedThreadPool(poolSize);
		System.setProperty("http.agent", "Mozilla/5.0");
		this.timeout=timeout;
		this.timeUtil=timeUtil;
	}
	public List<RSSFeed> get(Optional<String[]> urls,Locale locale)
	{
		List<RSSFeed> list=new LinkedList();
		if(urls.isPresent())
		{
			fillList(list,urls.get(),locale);
			list.sort(comparator);
		}
		return list;
	}
	private void fillList(List<RSSFeed> list,String[] urls,Locale locale)
	{
		Date now=new Date();
		List<Callable<List<RSSFeed>>> callables=new LinkedList<>();
		for(String url:urls)
			callables.add(()->getListFromUrl(url,now,locale));
		try
		{
			List<Future<List<RSSFeed>>> futures=executorService.invokeAll(callables);
			for(Future<List<RSSFeed>> future:futures)
				list.addAll(future.get());
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE,"Error",e);
		}
	}
	private List<RSSFeed> getListFromUrl(String urlString,Date now,Locale locale)
	{
		List<RSSFeed> list=new LinkedList<>();
		try
		{
			DocumentBuilder documentBuilder=documentBuilderFactory.newDocumentBuilder();
			URL url=new URL(urlString);
            URLConnection connection=url.openConnection();
			connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
			Document document=documentBuilder.parse(connection.getInputStream());
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
					rssFeed.setTimeDifference(timeUtil.timeDifference(rssFeed.getPubDate(),locale));
					rssFeed.setImage(getImage(element));
					list.add(rssFeed);
				}
			}
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE,urlString,e);
		}
		return list;
	}
	private String parseTag(Element element,String tag)
	{
		NodeList nodeList=element.getElementsByTagName(tag);
		if(nodeList.getLength()>0)
			return nodeList.item(0).getTextContent();
		return null;
	}
	private String getImage(Element element)
	{
		NodeList enclosureNodeList=element.getElementsByTagName("enclosure");
		if(enclosureNodeList.getLength()>0)
			return enclosureNodeList.item(0).getAttributes().getNamedItem("url").getNodeValue();
		NodeList mediaThumbnailNodeList=element.getElementsByTagName("media:thumbnail");
		if(mediaThumbnailNodeList.getLength()>0)
			return mediaThumbnailNodeList.item(0).getAttributes().getNamedItem("url").getNodeValue();
		return null;
	}
}