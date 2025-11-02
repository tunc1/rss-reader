package app.util;

import java.util.Date;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class TimeUtil
{
	private final MessageSource messageSource;
	public TimeUtil(MessageSource messageSource)
	{
		this.messageSource=messageSource;
	}
	public String timeDifference(Date pubDate,Locale locale)
	{
		Date now=new Date();
		long seconds=(now.getTime()-pubDate.getTime())/1000;
		if(seconds>86400*2)
			return messageSource.getMessage("days-ago",new Object[]{seconds/86400},locale);
		if(seconds>=86400)
			return messageSource.getMessage("one-day-ago",null,locale);
		if(seconds>=7200)
			return messageSource.getMessage("hours-ago",new Object[]{seconds/3600},locale);
		if(seconds>3600)
			return messageSource.getMessage("one-hour-ago",null,locale);
		if(seconds>=120)
			return messageSource.getMessage("minutes-ago",new Object[]{seconds/60},locale);
		if(seconds>60)
			return messageSource.getMessage("one-minute-ago",null,locale);
		return messageSource.getMessage("just-now",null,locale);
	}
}