package app.util;

import java.util.Date;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import java.net.SocketTimeoutException;

@Component
public class ErrorMessageUtil
{
	private final MessageSource messageSource;
	public ErrorMessageUtil(MessageSource messageSource)
	{
		this.messageSource=messageSource;
	}
	public String generic(Locale locale)
	{
		return messageSource.getMessage("generic-error",null,locale);
	}
	public String couldNotConnect(Locale locale)
	{
		return messageSource.getMessage("could-not-connect",null,locale);
	}
	public String invalidUrl(Locale locale)
	{
		return messageSource.getMessage("invalid-url",null,locale);
	}
	public String invalidRSS(Locale locale)
	{
		return messageSource.getMessage("invalid-rss",null,locale);
	}
}