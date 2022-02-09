package it.at7.gemini.micronaut.api;

import io.micronaut.http.*;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.codec.CodecException;
import it.at7.gemini.micronaut.core.*;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RequestUtils {

    public static GeminiHttpResponse errorResponseLogger(HttpRequest request, String errorMessage) {
        return errorResponseLogger(request, errorMessage, null, null);
    }

    public static GeminiHttpResponse errorResponseLogger(HttpRequest request, Throwable e) {
        return errorResponseLogger(request, e.getMessage(), null, e);
    }

    public static GeminiHttpResponse errorResponseLogger(HttpRequest request, String errorMessage, @Nullable Map<String, Object> additionalInfo, @Nullable Throwable e) {
        Optional<TimeWatchLogger> time_logger = request.getAttribute("TIME_LOGGER", TimeWatchLogger.class);
        GeminiHttpResponse errorBody = GeminiHttpResponse.error(errorMessage, additionalInfo);
        time_logger.ifPresent(t -> errorBody.addElapsedTime(t.error("Error response ready: " + errorMessage, e)));
        return errorBody;
    }

    public static TimeWatchLogger createAndSetTimeLogger(Logger logger, HttpRequest httpRequest, String tag, String message) {
        TimeWatchLogger tlogger = TimeWatchLogger.info(logger, tag, message);
        httpRequest.setAttribute("TIME_LOGGER", tlogger);
        return tlogger;
    }

    public static boolean isDataMap(DataRequest body) {
        return body.getData() instanceof Map;
    }

    public static Map<String, Object> getRequestDataMap(@Body DataRequest body) {
        if (body.getData() == null)
            throw new CodecException("data field not found in request body");
        if (!(body.getData() instanceof Map))
            throw new RuntimeException("data field must be an object");
        return (Map<String, Object>) body.getData();
    }

    public static List<Map<String, Object>> getRequestDataList(@Body DataRequest body) {
        if (body.getData() == null)
            throw new CodecException("data field not found in request body");
        if (!(body.getData() instanceof Collection))
            throw new RuntimeException("data field must be an object");
        return (List<Map<String, Object>>) body.getData();
    }

    public static HttpResponse<GeminiHttpResponse> readyResponse(DataResult<EntityRecord> result, HttpRequest request) {
        Map<String, Object> bodyData = ResponseConverter.convert(result.getData());
        return readyResponse(result, bodyData, request, null);
    }

    public static HttpResponse<GeminiHttpResponse> readyResponse(DataListResult<EntityRecord> result, HttpRequest request, @Nullable DataListRequest dataListRequest) {
        List<Map<String, Object>> bodyData = result.getData().stream().map(ResponseConverter::convert).collect(Collectors.toList());
        return readyResponse(result, bodyData, request, dataListRequest);
    }


    private static HttpResponse<GeminiHttpResponse> readyResponse(CommonResult result, Object dataBody, HttpRequest request, @Nullable DataListRequest dataListRequest) {
        GeminiHttpResponse responseBody = GeminiHttpResponse.success(dataBody);
        MutableHttpResponse<GeminiHttpResponse> resp = okResponse(responseBody);
        result.getLastUpdateTime().ifPresent(updatedTime -> {
            responseBody.addLastUpdate(updatedTime);
            resp.header(HttpHeaders.ETAG, String.valueOf(updatedTime));
        });
        if (dataListRequest != null & dataListRequest.getLimit() > 0) {
            responseBody.addMeta("limit", dataListRequest.getLimit());
            responseBody.addMeta("start", dataListRequest.getStart());
        }
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
