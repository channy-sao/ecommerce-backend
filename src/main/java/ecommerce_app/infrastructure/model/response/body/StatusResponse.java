package ecommerce_app.infrastructure.model.response.body;

import ecommerce_app.constant.message.ResponseMessageConstant;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

public class StatusResponse implements Serializable {

  @Schema(example = "200")
  public int code;

  @Schema(example = "Success")
  public String message;

  public StatusResponse(int code, String message) {
    this.code = code;
    this.message = message != null ? message : ResponseMessageConstant.FAILED;
  }
}