package ecommerce_app.api.admin;

import ecommerce_app.constant.message.ResponseMessageConstant;
import ecommerce_app.infrastructure.model.response.body.BaseBodyResponse;
import ecommerce_app.modules.setting.model.dto.SettingDto;
import ecommerce_app.modules.setting.service.SettingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/v1/settings")
@RequiredArgsConstructor
@Tag(name = "Settings", description = "System configuration")
public class SettingController {

    private final SettingService settingService;

    @GetMapping
    public ResponseEntity<BaseBodyResponse<List<SettingDto.Response>>> getAll() {
        return BaseBodyResponse.success(
            settingService.getAll(),
            ResponseMessageConstant.FIND_ALL_SUCCESSFULLY);
    }

    @PatchMapping("/{key}")
    public ResponseEntity<BaseBodyResponse<SettingDto.Response>> update(
            @PathVariable String key,
            @Valid @RequestBody SettingDto.UpdateRequest request) {
        return BaseBodyResponse.success(
            settingService.update(key, request.getValue()),
            ResponseMessageConstant.UPDATE_SUCCESSFULLY);
    }

    @PatchMapping("/bulk")
    public ResponseEntity<BaseBodyResponse<List<SettingDto.Response>>> bulkUpdate(
            @Valid @RequestBody SettingDto.BulkUpdateRequest request) {
        return BaseBodyResponse.success(
            settingService.bulkUpdate(request.getSettings()),
            ResponseMessageConstant.UPDATE_SUCCESSFULLY);
    }
}