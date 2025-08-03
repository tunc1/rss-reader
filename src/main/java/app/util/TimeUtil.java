package app.util;

import java.util.Date;

public class TimeUtil
{
	public static String timeDifference(Date pubDate)
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