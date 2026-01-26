package ecommerce_app.infrastructure.model.response.body;

import com.fasterxml.jackson.annotation.JsonInclude;
import ecommerce_app.constant.message.ResponseMessageConstant;
import ecommerce_app.util.StatusResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static ecommerce_app.config.TraceFilter.PATH;
import static ecommerce_app.config.TraceFilter.TRACE_ID;
import static ecommerce_app.constant.app.AppConstant.REQUEST_ID;

/**
 * Standard API response wrapper used across the application.
 *
 * <p>This class provides a unified response structure for successful and failed HTTP responses,
 * including metadata such as timestamp, trace ID, and request path.
 *
 * <p>Typical response structure:
 *
 * <ul>
 *   <li>{@code success} - indicates whether the request was successful
 *   <li>{@code data} - response payload (if any)
 *   <li>{@code meta} - pagination metadata for paged responses
 *   <li>{@code status} - HTTP status and message
 *   <li>{@code timestamp} - response creation time
 *   <li>{@code traceId} - request trace identifier
 *   <li>{@code path} - request URI path
 * </ul>
 */
@Getter
@Setter
public class BaseBodyResponse implements Serializable {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean success;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Object data;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private PageResponse meta;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private StatusResponse status;

  private String timestamp;

  private String traceId;

  private String requestId;

  private String path;

  /* =========================
   * Success Responses
   * ========================= */

  /**
   * Creates a successful response containing a data payload.
   *
   * @param data the response body data
   * @param message optional success message
   * @return {@link ResponseEntity} wrapping {@link BaseBodyResponse}
   */
  public static ResponseEntity<BaseBodyResponse> success(Object data, String message) {
    BaseBodyResponse response = buildSuccess(message);
    response.setData(data);
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
  }

  /**
   * Creates a successful response containing a list of data payloads.
   *
   * @param data the list of response body data
   * @param message optional success message
   * @return {@link ResponseEntity} wrapping {@link BaseBodyResponse}
   */
  public static ResponseEntity<BaseBodyResponse> success(List<?> data, String message) {
    short statusCode = 200;
    BaseBodyResponse baseBodyResponse = new BaseBodyResponse();

    StatusResponse statusResponse =
            new StatusResponse(statusCode, message != null ? message : ResponseMessageConstant.SUCCESS);

    baseBodyResponse.setSuccess(true);
    baseBodyResponse.setStatus(statusResponse);
    baseBodyResponse.setData(data);
    populateTraceAndPath(baseBodyResponse);
    return ResponseEntity.status(statusCode).body(baseBodyResponse);
  }

  /**
   * Creates a successful response without a data payload.
   *
   * @param message optional success message
   * @return {@link ResponseEntity} wrapping {@link BaseBodyResponse}
   */
  public static ResponseEntity<BaseBodyResponse> success(String message) {
    BaseBodyResponse response = buildSuccess(message);
    return ResponseEntity.ok(response);
  }

  /**
   * Creates a successful paginated response.
   *
   * @param page the Spring {@link Page} result
   * @param message optional success message
   * @return {@link ResponseEntity} wrapping {@link BaseBodyResponse}
   */
  public static ResponseEntity<BaseBodyResponse> pageSuccess(Page<?> page, String message) {
    BaseBodyResponse response = buildSuccess(message);
    response.setData(page.getContent());
    response.setMeta(page.getPageable().isUnpaged() ? null : new PageResponse(page));
    return ResponseEntity.ok(response);
  }

  /* =========================
   * Failure Responses
   * ========================= */

  /**
   * Creates a failed response entity with the given HTTP status.
   *
   * @param status HTTP status code
   * @param message error message
   * @return {@link ResponseEntity} wrapping {@link BaseBodyResponse}
   */
  public static ResponseEntity<BaseBodyResponse> failed(HttpStatusCode status, String message) {
    BaseBodyResponse response = buildFailure(status, message);
    return ResponseEntity.status(status).body(response);
  }

  /**
   * Creates a failed response body without wrapping it in {@link ResponseEntity}.
   *
   * @param status HTTP status code
   * @param message error message
   * @return {@link BaseBodyResponse}
   */
  // USING WHEN THROW EXCEPTION IN CONTROLLER ADVICE
  public static BaseBodyResponse bodyFailed(HttpStatusCode status, String message) {
    return buildFailure(status, message);
  }

  /* =========================
   * Internal Builders
   * ========================= */

  private static BaseBodyResponse buildSuccess(String message) {
    BaseBodyResponse response = new BaseBodyResponse();
    response.setSuccess(true);
    response.setStatus(
            new StatusResponse(
                    HttpStatus.OK.value(), message != null ? message : ResponseMessageConstant.SUCCESS));
    populateTraceAndPath(response);
    return response;
  }

  private static BaseBodyResponse buildFailure(HttpStatusCode status, String message) {
    BaseBodyResponse response = new BaseBodyResponse();
    response.setSuccess(false);
    response.setStatus(StatusResponseUtils.createStatusResponse(status, message));
    populateTraceAndPath(response);
    return response;
  }

  /**
   * Populates tracing and request-related metadata into the given response object.
   *
   * <p>This method retrieves the current {@link HttpServletRequest} from the {@link
   * RequestContextHolder} and extracts request-scoped attributes such as trace ID and request path.
   * It also sets the current timestamp.
   *
   * <p>If no request context is available (e.g. when called outside an HTTP request), the response
   * is left unchanged.
   *
   * @param response the response object to populate with trace ID, path, and timestamp
   */
  private static void populateTraceAndPath(BaseBodyResponse response) {
    ServletRequestAttributes attrs =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attrs != null) {
      HttpServletRequest request = attrs.getRequest();
      response.setTraceId((String) request.getAttribute(TRACE_ID));
      response.setPath((String) request.getAttribute(PATH));
      response.setTimestamp(String.valueOf(Instant.now()));
      response.setRequestId((String) request.getAttribute(REQUEST_ID));
    }
  }
}
