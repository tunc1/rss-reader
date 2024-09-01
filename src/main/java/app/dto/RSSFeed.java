package app.dto;

import java.util.Date;

public class RSSFeed
{
	private String title,link,source,timeDifference;
	private Date pubDate;
	public String getTitle()
	{
		return title;
	}
	public void setTitle(String title)
	{
		this.title=title;
	}
	public String getLink()
	{
		return link;
	}
	public void setLink(String link)
	{
		this.link=link;
	}
	public String getSource()
	{
		return source;
	}
	public void setSource(String source)
	{
		this.source=source;
	}
	public String getTimeDifference()
	{
		return timeDifference;
	}
	public void setTimeDifference(String timeDifference)
	{
		this.timeDifference=timeDifference;
	}
	public Date getPubDate()
	{
		return pubDate;
	}
	public void setPubDate(Date pubDate)
	{
		this.pubDate=pubDate;
	}
}