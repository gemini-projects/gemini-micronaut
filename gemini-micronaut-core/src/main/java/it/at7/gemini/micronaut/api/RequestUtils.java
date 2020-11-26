package it.at7.gemini.micronaut.api;

import io.micronaut.http.*;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.codec.CodecException;
import it.at7.gemini.micronaut.core.*;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RequestUtils {

    public static GeminiHttpResponse errorResponseLogger(HttpRequest request, String errorMessage) {
        return errorResponseLogger(request, errorMessage, null);
    }

    public static GeminiHttpResponse errorResponseLogger(HttpRequest request, String errorMessage, @Nullable Map<String, Object> additionalInfo) {
        Optional<TimeWatchLogger> time_logger = request.getAttribute("TIME_LOGGER", TimeWatchLogger.class);
        GeminiHttpResponse errorBody = GeminiHttpResponse.error(errorMessage, additionalInfo);
        time_logger.ifPresent(t -> errorBody.addElapsedTime(t.error("Error response ready: " + errorMessage)));
        return errorBody;
    }

    public static TimeWatchLogger crateAndSetTimeLogger(Logger logger, HttpRequest httpRequest, String tag, String message) {
        TimeWatchLogger tlogger = TimeWatchLogger.info(logger, tag, message);
        httpRequest.setAttribute("TIME_LOGGER", tlogger);
        return tlogger;
    }

    public static Map<String, Object> getRequestData(@Body DataRequest body) {
        Map<String, Object> data = body.getData();
        if (data == null)
            throw new CodecException("data field not found in request body");
        return data;
    }

    public static HttpResponse<GeminiHttpResponse> readyResponse(DataResult<EntityRecord> result, HttpRequest request) {
        Map<String, Object> bodyData = result.getData().getData();
        return readyResponse(result, bodyData, request);
    }

    public static HttpResponse<GeminiHttpResponse> readyResponse(DataListResult<EntityRecord> result, HttpRequest request) {
        List<Map<String, Object>> bodyData = result.getData().stream().map(EntityRecord::getData).collect(Collectors.toList());
        return readyResponse(result, bodyData, request);
    }


    private static HttpResponse<GeminiHttpResponse> readyResponse(CommonResult result, Object dataBody, HttpRequest request) {
        GeminiHttpResponse responseBody = GeminiHttpResponse.success(dataBody);
        MutableHttpResponse<GeminiHttpResponse> resp = okResponse(responseBody);
        result.getLastUpdateTime().ifPresent(updatedTime -> resp.header(HttpHeaders.ETAG, String.valueOf(updatedTime)));
        Optional<TimeWatchLogger> time_logger = request.getAttribute("TIME_LOGGER", TimeWatchLogger.class);
        time_logger.ifPresent(t -> responseBody.addElapsedTime(t.info("Response Ready")));
        return resp;
    }

    public static HttpResponse<GeminiHttpResponse> readyResponse(Object dataBody, HttpRequest request) {
        GeminiHttpResponse responseBody = GeminiHttpResponse.success(dataBody);
        MutableHttpResponse<GeminiHttpResponse> resp = okResponse(responseBody);
        Optional<TimeWatchLogger> time_logger = request.getAttribute("TIME_LOGGER", TimeWatchLogger.class);
        time_logger.ifPresent(t -> responseBody.addElapsedTime(t.info("Response Ready")));
        return resp;
    }

    public static MutableHttpResponse<GeminiHttpResponse> okResponse(GeminiHttpResponse response) {
        MutableHttpResponse<GeminiHttpResponse> resp = HttpResponse.status(HttpStatus.OK);
        resp.body(response);
        return resp;
    }

}
