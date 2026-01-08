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

  private String path;

  public static ResponseEntity<BaseBodyResponse> success(Object data, String message) {
    return buildSuccessResponse(data, message);
  }

  private static ResponseEntity<BaseBodyResponse> buildSuccessResponse(
      Object data, String message) {
    BaseBodyResponse baseBodyResponse = new BaseBodyResponse();
    baseBodyResponse.setSuccess(true);
    baseBodyResponse.setStatus(
        new StatusResponse(
            HttpStatus.OK.value(), message != null ? message : ResponseMessageConstant.SUCCESS));
    baseBodyResponse.setData(data);
    populateTraceAndPath(baseBodyResponse);

    ResponseEntity.BodyBuilder responseBuilder =
        ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON);

    return responseBuilder.body(baseBodyResponse);
  }

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

  public static ResponseEntity<BaseBodyResponse> success(String message) {
    short statusCode = 200;
    BaseBodyResponse baseBodyResponse = new BaseBodyResponse();

    StatusResponse statusResponse =
        new StatusResponse(statusCode, message != null ? message : ResponseMessageConstant.SUCCESS);

    baseBodyResponse.setSuccess(true);
    baseBodyResponse.setStatus(statusResponse);
    populateTraceAndPath(baseBodyResponse);

    return ResponseEntity.status(statusCode).body(baseBodyResponse);
  }

  public static ResponseEntity<BaseBodyResponse> pageSuccess(Page<?> page, String message) {
    short statusCode = 200;
    BaseBodyResponse baseBodyResponse = new BaseBodyResponse();

    PageResponse pageResponse;
    if (page.getPageable().isUnpaged()) pageResponse = null;
    else pageResponse = new PageResponse(page);

    StatusResponse statusResponse =
        new StatusResponse(statusCode, message != null ? message : ResponseMessageConstant.SUCCESS);

    baseBodyResponse.setSuccess(true);
    baseBodyResponse.setStatus(statusResponse);
    baseBodyResponse.setData(page.getContent());
    baseBodyResponse.setMeta(pageResponse);
    populateTraceAndPath(baseBodyResponse);

    return ResponseEntity.status(statusCode).body(baseBodyResponse);
  }

  public static ResponseEntity<BaseBodyResponse> failed(HttpStatusCode status, String message) {

    BaseBodyResponse bodyResponse = new BaseBodyResponse();
    bodyResponse.setSuccess(false);
    bodyResponse.setStatus(StatusResponseUtils.createStatusResponse(status, message));
    populateTraceAndPath(bodyResponse);
    return ResponseEntity.status(status).body(bodyResponse);
  }

  public static ResponseEntity<Object> internalFailed(HttpStatusCode status, String message) {
    BaseBodyResponse bodyResponse = new BaseBodyResponse();
    bodyResponse.setSuccess(false);
    bodyResponse.setStatus(StatusResponseUtils.createStatusResponse(status, message));
    populateTraceAndPath(bodyResponse);
    return ResponseEntity.status(status).body(bodyResponse);
  }

  public static BaseBodyResponse bodyFailed(HttpStatusCode status, String message) {

    StatusResponse statusResponse = StatusResponseUtils.createStatusResponse(status, message);

    BaseBodyResponse bodyResponse = new BaseBodyResponse();
    bodyResponse.setSuccess(false);
    bodyResponse.setStatus(statusResponse);
    populateTraceAndPath(bodyResponse);

    return bodyResponse;
  }

  private static void populateTraceAndPath(BaseBodyResponse response) {
    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attrs != null) {
      HttpServletRequest request = attrs.getRequest();
      response.setTraceId((String) request.getAttribute("traceId"));
      response.setPath((String) request.getAttribute("path"));
      response.setTimestamp(String.valueOf(Instant.now()));
    }
  }
}
