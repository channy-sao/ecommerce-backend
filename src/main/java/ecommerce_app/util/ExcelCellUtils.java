package ecommerce_app.util;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExcelCellUtils {
  public static String getStringCell(Cell cell) {
    if (cell == null) return null;
    return cell.getStringCellValue().trim();
  }

  public static BigDecimal getBigDecimalCell(Cell cell) {
    if (cell == null) return BigDecimal.ZERO;
    return BigDecimal.valueOf(cell.getNumericCellValue());
  }

  public static Long getLongCell(Cell cell) {
    if (cell == null) return null;
    return (long) cell.getNumericCellValue();
  }

  public static Boolean getBooleanCell(Cell cell) {
    if (cell == null) return false;
    return Boolean.valueOf(cell.getStringCellValue());
  }
}
