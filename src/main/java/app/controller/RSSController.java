package app.controller;

import org.springframework.web.bind.annotation.*;
import app.service.RSSParser;
import app.controller.response.RSSResponse;
import app.dto.RSSFeed;
import java.util.List;
import java.util.Optional;
import java.util.Locale;

@RestController
@RequestMapping("/api/rss")
@CrossOrigin
public class RSSController
{
	private RSSParser parser;
	public RSSController(RSSParser parser)
	{
		this.parser=parser;
	}
	@GetMapping
	public RSSResponse get(Optional<String[]> urls,@RequestParam(defaultValue="en") Locale locale)
	{
		return parser.get(urls,locale);
	}
}
