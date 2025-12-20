package app.controller.response;

import app.dto.*;
import java.util.List;

public record RSSResponse(List<RSSFeed> list,List<ErrorMessage> errorMessages){}