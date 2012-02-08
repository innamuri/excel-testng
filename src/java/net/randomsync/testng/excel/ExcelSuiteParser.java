package net.randomsync.testng.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.testng.TestNGException;
import org.testng.xml.*;

public class ExcelSuiteParser implements IFileParser<XmlSuite> {

	private Map<String, Integer> excelTestDataMap;
	private File testClassesMap;

	/**
	 * Default constructor, initializes the test data map
	 * 
	 * @param xlSource
	 *            - the source Excel file
	 */
	public ExcelSuiteParser() {
		excelTestDataMap = new HashMap<String, Integer>();
		excelTestDataMap.put("headerRow", 0);
		excelTestDataMap.put("testIdCol", 0);
		excelTestDataMap.put("testNameCol", 1);
		excelTestDataMap.put("testDescCol", 2);
		excelTestDataMap.put("testParamCol", 3);
		excelTestDataMap.put("testConfigCol", 4);
	}

	public ExcelSuiteParser(Map<String, Integer> excelTestDataMap) {
		this.excelTestDataMap = excelTestDataMap;
	}

	/**
	 * Parses the Excel file into a TestNG XmlSuite and returns the suite. If
	 * there are multiple worksheets, all of them are parsed into a single
	 * XmlSuite and returned. In this implementation, the InputStream is ignored
	 * and only filePath is used
	 * 
	 * @param filePath
	 *            source Excel file or directory
	 * @param InputStream
	 *            input stream (not used)
	 * @param loadClasses
	 * @return a TestNG XmlSuite with all tests from the Excel file
	 * @throws TestNGException
	 * @see org.testng.xml.IFileParser#parse(java.lang.String,
	 *      java.io.InputStream, boolean)
	 */
	@Override
	public XmlSuite parse(String filePath, InputStream is, boolean loadClasses)
			throws TestNGException {
		// TODO use loadClasses to decide whether to load classes when creating
		// suites
		String name = "";
		File file = null;
		if (!filePath.isEmpty()) {
			file = new File(filePath);
			name = file.getName();
			name = name.substring(0, name.lastIndexOf("."));
		}
		ExcelTestSuite suite = new ExcelTestSuite(name);
		try {
			suite.setTestCases(parseExcelTestCases(file));
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return suite.getSuiteAsXmlSuite();
	}

	/**
	 * this is the main parser method that parses the Excel file and returns the
	 * tests within the file. Each row is considered as a test case with
	 * specific columns (specified by {@link #excelTestDataMap}) as test
	 * 
	 * @return a list of test cases in the Excel file
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	public List<ExcelTestCase> parseExcelTestCases(File src)
			throws InvalidFormatException, IOException {
		List<ExcelTestCase> testCases = new ArrayList<ExcelTestCase>();

		FileInputStream fis = new FileInputStream(src);
		Workbook wb = WorkbookFactory.create(fis);
		DataFormatter formatter = new DataFormatter();

		// validate values and assign default if needed
		int headerRow = getMapValue("headerRow", 0);
		int testIdCol = getMapValue("testIdCol", 0);

		/*
		 * parse each sheet starting from headerRow. Each row is a test case and
		 * needs to be added if test id is not blank
		 */
		for (int i = 0; i < wb.getNumberOfSheets(); i++) {
			Sheet sheet = wb.getSheetAt(i);
			Row row = null;
			for (int j = headerRow + 1; j <= sheet.getLastRowNum(); j++) {
				row = sheet.getRow(j);
				if (row != null) {
					if (!formatter.formatCellValue(row.getCell(testIdCol))
							.isEmpty()) {
						ExcelTestCase tc = null;
						try {
							tc = getExcelTestCaseFromRow(row, formatter);

						} catch (Exception e) {
							// TODO add specific exception handler
							e.printStackTrace();
							// skip the current row and continue
							continue;
						}
						testCases.add(tc);
					}

				}
			}
		}
		return testCases;
	}

	/**
	 * @param excelTestDataMap
	 *            - the Map that will be used to parse Excel file(s)
	 */
	public void setExcelTestDataMap(Map<String, Integer> excelTestDataMap) {
		this.excelTestDataMap = excelTestDataMap;
	}

	private ExcelTestCase getExcelTestCaseFromRow(Row row,
			DataFormatter formatter) {
		int testIdCol = getMapValue("testIdCol", 0);
		int testNameCol = getMapValue("testNameCol", 1);
		int testDescCol = getMapValue("testDescCol", 2);
		int testParamCol = getMapValue("testParamCol", 3);
		int testConfigCol = getMapValue("testConfigCol", 4);

		return new ExcelTestCase(formatter.formatCellValue(row
				.getCell(testIdCol)), // test id
				formatter.formatCellValue(row.getCell(testNameCol)), // test
																		// name
				formatter.formatCellValue(row.getCell(testDescCol)), // description
				formatter.formatCellValue(row.getCell(testParamCol)), // parameters
				formatter.formatCellValue(row.getCell(testConfigCol)) // configuration
		);

	}

	private int getMapValue(String key, int defaultVal) {
		int value;
		try {
			value = this.excelTestDataMap.get(key);
		} catch (NullPointerException npe) {
			return defaultVal;
		}
		return value;
	}
}