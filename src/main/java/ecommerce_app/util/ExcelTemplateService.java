package ecommerce_app.util;

import com.github.javafaker.Faker;
import ecommerce_app.infrastructure.exception.BadRequestException;
import ecommerce_app.modules.category.model.entity.Category;
import ecommerce_app.modules.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class ExcelTemplateService {
  private final CategoryRepository categoryRepository;
  private final Faker faker = new Faker();
  private final Random random = new Random();

  /** Generate product import template with dynamic sample data using Faker */
  public byte[] generateProductImportTemplate(int numberOfRows, boolean includeSampleData) {
    try (XSSFWorkbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

      // Create main sheet
      XSSFSheet sheet = workbook.createSheet("Products");

      // Create styles
      CellStyle headerStyle = createHeaderStyle(workbook);
      CellStyle requiredStyle = createRequiredHeaderStyle(workbook);
      CellStyle sampleDataStyle = createSampleDataStyle(workbook);
      CellStyle instructionStyle = createInstructionStyle(workbook);

      // Get available categories
      List<Long> categoryIds = categoryRepository.findAll().stream().map(Category::getId).toList();

      // Create header row
      createHeaderRow(sheet, headerStyle, requiredStyle);

      // Create instruction sheet
      createInstructionSheet(workbook, instructionStyle, categoryIds);

      // Add sample data if requested
      if (includeSampleData && numberOfRows > 0) {
        addFakerSampleData(sheet, sampleDataStyle, numberOfRows, categoryIds);
      }

      // Set column widths
      setColumnWidths(sheet);

      // Add data validation
      addDataValidation(sheet, numberOfRows, categoryIds);

      // Freeze header row
      sheet.createFreezePane(0, 1);

      workbook.write(outputStream);
      log.info("Generated Excel template with {} sample rows", numberOfRows);
      return outputStream.toByteArray();
    } catch (Exception e) {
      log.error("Error generating Excel template: {}", e.getMessage(), e);
      throw new BadRequestException(e.getMessage());
    }
  }

  /** Create header row with column names matching ProductRequest */
  private void createHeaderRow(XSSFSheet sheet, CellStyle headerStyle, CellStyle requiredStyle) {
    Row headerRow = sheet.createRow(0);

    String[] headers = {
      "Name*", // Column 0
      "Description", // Column 1
      "Price*", // Column 2
      "Image URL", // Column 3 (will be handled separately in import)
      "Category ID*", // Column 4
      "Is Feature" // Column 5
    };

    boolean[] required = {true, false, true, false, true, false};

    for (int i = 0; i < headers.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(headers[i]);
      cell.setCellStyle(required[i] ? requiredStyle : headerStyle);
    }
  }

  /** Add sample data using Faker library */
  private void addFakerSampleData(
      XSSFSheet sheet, CellStyle sampleDataStyle, int numberOfRows, List<Long> categoryIds) {

    for (int i = 0; i < numberOfRows; i++) {
      Row row = sheet.createRow(i + 1);

      // Generate random product data using Faker
      String productName = generateProductName();
      String description = faker.commerce().productName() + " - " + faker.lorem().sentence(10);
      BigDecimal price = generatePrice();

      // Image URL (Column 3)
      String imageUrl = "https://via.placeholder.com/800x600?text=" + productName.replace(" ", "+");

      long categoryId =
          categoryIds.isEmpty() ? 1L : categoryIds.get(random.nextInt(categoryIds.size()));
      boolean isFeature = random.nextBoolean();

      // Name (Column 0)
      Cell nameCell = row.createCell(0);
      nameCell.setCellValue(productName);
      nameCell.setCellStyle(sampleDataStyle);

      // Description (Column 1)
      Cell descCell = row.createCell(1);
      descCell.setCellValue(description);
      descCell.setCellStyle(sampleDataStyle);

      // Price (Column 2)
      Cell priceCell = row.createCell(2);
      priceCell.setCellValue(price.doubleValue());
      priceCell.setCellStyle(sampleDataStyle);

      // Image URL (Column 3)
      Cell imageCell = row.createCell(3);
      imageCell.setCellValue(imageUrl);
      imageCell.setCellStyle(sampleDataStyle);

      // Category ID (Column 4)
      Cell categoryCell = row.createCell(4);
      categoryCell.setCellValue(categoryId);
      categoryCell.setCellStyle(sampleDataStyle);

      // Is Feature (Column 5)
      Cell featureCell = row.createCell(5);
      featureCell.setCellValue(isFeature ? "TRUE" : "FALSE");
      featureCell.setCellStyle(sampleDataStyle);
    }
  }

  /** Generate realistic product names */
  private String generateProductName() {
    String[] brands = {
      "Apple", "Samsung", "Sony", "Nike", "Adidas", "Dell",
      "HP", "Lenovo", "Microsoft", "Google", "Amazon", "LG"
    };

    String[] adjectives = {
      "Pro", "Max", "Plus", "Elite", "Premium", "Ultra", "Advanced", "Smart", "Classic", "Modern"
    };

    String brand = brands[random.nextInt(brands.length)];
    String product = faker.commerce().productName();
    String adjective = adjectives[random.nextInt(adjectives.length)];

    return brand + " " + product + " " + adjective;
  }

  /** Generate realistic prices */
  private BigDecimal generatePrice() {
    double basePrice = 10 + (random.nextDouble() * 2990); // $10 - $3000
    return BigDecimal.valueOf(basePrice).setScale(2, RoundingMode.HALF_UP);
  }

  /** Create instruction sheet with detailed guidelines */
  private void createInstructionSheet(
      XSSFWorkbook workbook, CellStyle instructionStyle, List<Long> categoryIds) {
    XSSFSheet instructionSheet = workbook.createSheet("Instructions");

    String categoryIdsStr =
        categoryIds.isEmpty()
            ? "Please create categories first"
            : categoryIds.stream().map(String::valueOf).collect(Collectors.joining(", "));

    String[] instructions = {
      "PRODUCT IMPORT TEMPLATE - INSTRUCTIONS",
      "",
      "COLUMN MAPPING:",
      "  Column A (0): Name* - Product name",
      "  Column B (1): Description - Product description",
      "  Column C (2): Price* - Product price",
      "  Column D (3): Image URL - Image URL (optional, images handled separately)",
      "  Column E (4): Category ID* - Existing category ID",
      "  Column F (5): Is Feature - TRUE/FALSE",
      "",
      "REQUIRED FIELDS (marked with *):",
      "  • Name: Product name (must be unique)",
      "  • Price: Product price (must be positive number)",
      "  • Category ID: Must match existing category ID",
      "",
      "OPTIONAL FIELDS:",
      "  • Description: Product description",
      "  • Image URL: Image URL (for reference only, upload images separately)",
      "  • Is Feature: TRUE or FALSE (defaults to FALSE if empty)",
      "",
      "AVAILABLE CATEGORY IDs:",
      "  " + categoryIdsStr,
      "",
      "FORMATTING GUIDELINES:",
      "  • Price: Use decimal format (e.g., 99.99, not $99.99)",
      "  • Category ID: Must be a valid integer from available categories",
      "  • Is Feature: Use TRUE/FALSE, Yes/No, or 1/0",
      "  • Image URL: For reference only - actual images uploaded separately",
      "",
      "DATA TYPES:",
      "  • Name: Text (String)",
      "  • Description: Text (String)",
      "  • Price: Number (Decimal)",
      "  • Image URL: Text (URL format)",
      "  • Category ID: Number (Long/Integer)",
      "  • Is Feature: Boolean (TRUE/FALSE)",
      "",
      "TIPS FOR SUCCESS:",
      "  • Do not modify column headers or their order",
      "  • Keep first row (headers) intact",
      "  • Remove sample data before adding your products",
      "  • Check for duplicate product names",
      "  • Verify category IDs exist in the system",
      "  • Test with a small batch first (5-10 products)",
      "",
      "ERROR HANDLING:",
      "  • Invalid rows will be reported with specific error messages",
      "  • Valid products will be imported even if some rows fail",
      "  • Review the import result for detailed error information",
      "",
      "SAMPLE DATA:",
      "  • Generated using Faker library for realistic test data",
      "  • Replace with your actual product data before import",
      "",
      "For support, contact: support@example.com"
    };

    for (int i = 0; i < instructions.length; i++) {
      Row row = instructionSheet.createRow(i);
      Cell cell = row.createCell(0);
      cell.setCellValue(instructions[i]);

      if (i == 0) {
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        titleStyle.setFont(titleFont);
        cell.setCellStyle(titleStyle);
      } else {
        cell.setCellStyle(instructionStyle);
      }
    }

    instructionSheet.setColumnWidth(0, 100 * 256);
  }

  /** Set column widths for better readability */
  private void setColumnWidths(XSSFSheet sheet) {
    sheet.setColumnWidth(0, 35 * 256); // Name
    sheet.setColumnWidth(1, 60 * 256); // Description
    sheet.setColumnWidth(2, 15 * 256); // Price
    sheet.setColumnWidth(3, 50 * 256); // Image URL
    sheet.setColumnWidth(4, 15 * 256); // Category ID
    sheet.setColumnWidth(5, 15 * 256); // Is Feature
  }

  /** Add data validation for specific columns */
  private void addDataValidation(XSSFSheet sheet, int numberOfRows, List<Long> categoryIds) {
    DataValidationHelper validationHelper = sheet.getDataValidationHelper();
    int maxRow = Math.max(numberOfRows + 100, 1000); // Allow room for more data

    // Is Feature column (Column 5) - dropdown with TRUE/FALSE
    CellRangeAddressList isFeaturedRange = new CellRangeAddressList(1, maxRow, 5, 5);
    DataValidationConstraint isFeaturedConstraint =
        validationHelper.createExplicitListConstraint(
            new String[] {"TRUE", "FALSE", "Yes", "No", "1", "0"});
    DataValidation isFeaturedValidation =
        validationHelper.createValidation(isFeaturedConstraint, isFeaturedRange);
    isFeaturedValidation.setShowErrorBox(true);
    isFeaturedValidation.createErrorBox("Invalid Value", "Please select TRUE or FALSE");
    sheet.addValidationData(isFeaturedValidation);

    // Price column (Column 2) - must be positive number
    CellRangeAddressList priceRange = new CellRangeAddressList(1, maxRow, 2, 2);
    DataValidationConstraint priceConstraint =
        validationHelper.createDecimalConstraint(
            DataValidationConstraint.OperatorType.GREATER_THAN, "0", null);
    DataValidation priceValidation = validationHelper.createValidation(priceConstraint, priceRange);
    priceValidation.setShowErrorBox(true);
    priceValidation.createErrorBox("Invalid Price", "Price must be a positive number");
    sheet.addValidationData(priceValidation);

    // Category ID column (Column 4) - must be positive integer
    if (!categoryIds.isEmpty()) {
      CellRangeAddressList categoryRange = new CellRangeAddressList(1, maxRow, 4, 4);

      // Create dropdown with available category IDs
      String[] categoryIdStrings = categoryIds.stream().map(String::valueOf).toArray(String[]::new);

      DataValidationConstraint categoryConstraint =
          validationHelper.createExplicitListConstraint(categoryIdStrings);
      DataValidation categoryValidation =
          validationHelper.createValidation(categoryConstraint, categoryRange);
      categoryValidation.setShowErrorBox(true);
      categoryValidation.createErrorBox(
          "Invalid Category", "Please select a valid Category ID from the list");
      sheet.addValidationData(categoryValidation);
    }
  }

  /** Create header cell style */
  private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    font.setFontHeightInPoints((short) 11);
    font.setColor(IndexedColors.WHITE.getIndex());
    style.setFont(font);
    style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    return style;
  }

  /** Create required header cell style (red background) */
  private CellStyle createRequiredHeaderStyle(XSSFWorkbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    font.setFontHeightInPoints((short) 11);
    font.setColor(IndexedColors.WHITE.getIndex());
    style.setFont(font);
    style.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    return style;
  }

  /** Create sample data cell style */
  private CellStyle createSampleDataStyle(XSSFWorkbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.setAlignment(HorizontalAlignment.LEFT);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    style.setWrapText(true);
    return style;
  }

  /** Create instruction cell style */
  private CellStyle createInstructionStyle(XSSFWorkbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setFontHeightInPoints((short) 10);
    style.setFont(font);
    style.setAlignment(HorizontalAlignment.LEFT);
    style.setVerticalAlignment(VerticalAlignment.TOP);
    style.setWrapText(true);
    return style;
  }
}
