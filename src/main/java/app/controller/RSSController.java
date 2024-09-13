package app.controller;

import org.springframework.web.bind.annotation.*;
import app.service.RSSParser;
import app.dto.RSSFeed;
import java.util.List;
import java.util.Optional;

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
	public List<RSSFeed> get(Optional<String[]> urls)
	{
		return parser.get(urls);
	}
}
