package app.service;

import java.util.List;
import java.util.LinkedList;
import java.util.Date;
import java.util.Comparator;
import java.util.Optional;
import java.util.Locale;
import java.text.SimpleDateFormat;
import app.dto.*;
import app.util.*;
import app.controller.response.RSSResponse;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXParseException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.net.SocketTimeoutException;

@Component
public class RSSParser
{
	private final Logger logger=Logger.getLogger(RSSParser.class.getName());
	private static final SimpleDateFormat format=new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
	private static final Comparator<RSSFeed> comparator=(r1,r2)->r2.getPubDate().compareTo(r1.getPubDate());
	private static final DocumentBuilderFactory documentBuilderFactory=DocumentBuilderFactory.newInstance();
	private final ExecutorService executorService;
	private final TimeUtil timeUtil;
	private final ErrorMessageUtil errorMessageUtil;
	private final int timeout;
	
	public RSSParser(@Value("${pool-size}") int poolSize,@Value("${timeout}") int timeout,TimeUtil timeUtil,ErrorMessageUtil errorMessageUtil)
	{
		executorService=Executors.newFixedThreadPool(poolSize);
		System.setProperty("http.agent", "Mozilla/5.0");
		this.timeout=timeout;
		this.timeUtil=timeUtil;
		this.errorMessageUtil=errorMessageUtil;
	}
	public RSSResponse get(Optional<String[]> urls,Locale locale)
	{
		List<RSSFeed> list=new LinkedList();
		List<ErrorMessage> errorMessages=new LinkedList();
		if(urls.isPresent())
		{
			fillList(list,errorMessages,urls.get(),locale);
			list.sort(comparator);
		}
		return new RSSResponse(list,errorMessages);
	}
	private void fillList(List<RSSFeed> list,List<ErrorMessage> errorMessages,String[] urls,Locale locale)
	{
		Date now=new Date();
		List<Callable<RSSFeedList>> callables=new LinkedList<>();
		for(String url:urls)
			callables.add(()->getListFromUrl(url,now,locale));
		try
		{
			List<Future<RSSFeedList>> futures=executorService.invokeAll(callables);
			for(Future<RSSFeedList> future:futures)
			{
				RSSFeedList rSSFeedList=future.get();
				list.addAll(rSSFeedList.rssFeeds());
				if(rSSFeedList.errorMessage().isPresent())
					errorMessages.add(rSSFeedList.errorMessage().get());
			}
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE,"Error",e);
		}
	}
	private RSSFeedList getListFromUrl(String urlString,Date now,Locale locale)
	{
		List<RSSFeed> list=new LinkedList<>();
		ErrorMessage errorMessage=null;
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
		catch (UnknownHostException e)
		{
			errorMessage=new ErrorMessage(urlString,errorMessageUtil.invalidUrl(locale));
		}
		catch (SocketTimeoutException e)
		{
			errorMessage=new ErrorMessage(urlString,errorMessageUtil.couldNotConnect(locale));
		}
		catch(SAXParseException e)
		{
			errorMessage=new ErrorMessage(urlString,errorMessageUtil.invalidRSS(locale));
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE,urlString,e);
			errorMessage=new ErrorMessage(urlString,errorMessageUtil.generic(locale));
		}
		return new RSSFeedList(list,Optional.ofNullable(errorMessage));
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