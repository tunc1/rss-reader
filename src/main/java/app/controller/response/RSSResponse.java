package app.controller.response;

import app.dto.RSSFeed;
import java.util.List;

public record RSSResponse(List<RSSFeed> list){}