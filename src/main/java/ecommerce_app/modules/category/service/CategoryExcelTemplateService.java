package ecommerce_app.modules.category.service;

import com.github.javafaker.Faker;
import ecommerce_app.constant.enums.ProductCategory;
import ecommerce_app.core.IExcelTempleBuilder;
import ecommerce_app.util.ExcelTemplateBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryExcelTemplateService implements IExcelTempleBuilder {
  private static final Map<ProductCategory, String[]> EMOJIS =
      Map.of(
          ProductCategory.ELECTRONICS, new String[] {"📱", "💻", "🖥️", "🎧", "🔌"},
          ProductCategory.FASHION, new String[] {"👕", "👗", "🧥", "👟", "👜"},
          ProductCategory.FOOD, new String[] {"🍔", "🍕", "🍎", "🥗", "☕"},
          ProductCategory.BOOKS, new String[] {"📚", "📖", "✏️"},
          ProductCategory.BEAUTY, new String[] {"💄", "🧴", "💅"},
          ProductCategory.HOME, new String[] {"🏠", "🛋️", "🪑", "🛏️"},
          ProductCategory.SPORTS, new String[] {"⚽", "🏀", "🏋️", "🎾"},
          ProductCategory.TOYS, new String[] {"🧸", "🪀", "🎲", "🚗"},
          ProductCategory.AUTOMOTIVE, new String[] {"🚗", "🏍️", "🔧", "⛽"});

  private final ExcelTemplateBuilder excelTemplateBuilder;
  private final Faker faker = new Faker();

  public byte[] generateCategoryExcelTemplate(int numberOfRows) {
    List<String> headers = List.of("Name*", "Description", "Icon", "Display Order");
    List<Boolean> required = List.of(true, false);
    return excelTemplateBuilder.buildTemplate(
        "Category",
        headers,
        required,
        numberOfRows,
        (sheet, rowIndex) -> fillSampleData(sheet, rowIndex, null),
        sheet -> addValidation(sheet, numberOfRows, null),
        this::addInstructions);
  }

  @Override
  public void fillSampleData(Sheet sheet, int rowIndex, List<Long> rangeIds) {
    Row row = sheet.createRow(rowIndex);
    String name = faker.company().name();
    row.createCell(0).setCellValue(name);
    row.createCell(1).setCellValue(faker.lorem().sentence(10));
    row.createCell(2)
        .setCellValue(
            EMOJIS
                .get(
                    ProductCategory.values()[
                        faker.number().numberBetween(0, ProductCategory.values().length)])[0]);
    row.createCell(3).setCellValue(faker.number().numberBetween(1, 100));
  }

  @Override
  public void addValidation(Sheet sheet, int numberOfRows, List<Long> rangeIds) {}

  private void addInstructions(XSSFWorkbook workbook) {
    String instruction = "Please fill in the category details. Fields marked with * are mandatory.";
    IExcelTempleBuilder.addInstructions(workbook, instruction);
  }
}
