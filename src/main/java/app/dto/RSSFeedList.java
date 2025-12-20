package app.dto;

import java.util.List;
import java.util.Optional;

public record RSSFeedList(List<RSSFeed> rssFeeds,Optional<ErrorMessage> errorMessage){}