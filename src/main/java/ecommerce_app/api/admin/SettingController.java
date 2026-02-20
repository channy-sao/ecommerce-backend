package ecommerce_app.api.admin;

import ecommerce_app.constant.message.MessageKeyConstant;
import ecommerce_app.constant.message.ResponseMessageConstant;
import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.setting.model.dto.SettingDto;
import ecommerce_app.modules.setting.service.SettingService;
import ecommerce_app.util.MessageSourceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/v1/settings")
@RequiredArgsConstructor
@Tag(name = "Settings", description = "System configuration")
public class SettingController {

  private final SettingService settingService;
  private final MessageSourceService messageSourceService;

  @PreAuthorize("hasAnyAuthority('SETTING_READ') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  @GetMapping
  public ResponseEntity<BaseBodyResponse<List<SettingDto.Response>>> getAll() {
    return BaseBodyResponse.success(
        settingService.getAll(), ResponseMessageConstant.FIND_ALL_SUCCESSFULLY);
  }

  @PreAuthorize(
      "hasAnyAuthority('SETTING_UPDATE', 'SETTING_CREATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  @PatchMapping("/{key}")
  public ResponseEntity<BaseBodyResponse<SettingDto.Response>> update(
      @PathVariable String key, @Valid @RequestBody SettingDto.UpdateRequest request) {
    return BaseBodyResponse.success(
        settingService.update(key, request.getValue()),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_UPDATE_SUCCESS));
  }

  @PreAuthorize(
      "hasAnyAuthority('SETTING_UPDATE', 'SETTING_CREATE') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  @PatchMapping("/bulk")
  public ResponseEntity<BaseBodyResponse<List<SettingDto.Response>>> bulkUpdate(
      @Valid @RequestBody SettingDto.BulkUpdateRequest request) {
    return BaseBodyResponse.success(
        settingService.bulkUpdate(request.getSettings()),
        messageSourceService.getMessage(MessageKeyConstant.COMMON_MESSAGE_UPDATE_SUCCESS));
  }
}
