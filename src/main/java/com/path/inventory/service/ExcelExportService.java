package com.path.inventory.service;

import com.path.inventory.entity.Device;
import com.path.inventory.repository.DeviceRepository;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService {

    private final DeviceRepository deviceRepository;

    public ExcelExportService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public void exportDevices(HttpServletResponse response) throws IOException {

        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=Devices.xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Devices");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row header = sheet.createRow(0);

        String[] columns = {
                "ID",
                "Tag Number",
                "Product Name",
                "Asset Category",
                "Serial Number",
                "Manufacturer",
                "Model No.",
                "Invoice No.",
                "Invoice Date",
                "Asset Age",
                "Asset Put Use Date",
                "Warranty Period",
                "Warranty Expiry Date",
                "Asset Location",
                "Asset Custodian",
                "Cost",
                "Available",
                "Assigned Employee",
                "Employee ID",
                "Issue Date"
        };

        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        List<Device> devices = deviceRepository.findAll();

        int rowCount = 1;

        for (Device device : devices) {

            Row row = sheet.createRow(rowCount++);

            row.createCell(0).setCellValue(device.getId());

            row.createCell(1).setCellValue(
                    device.getTagNumber() == null ? "" : device.getTagNumber());

            row.createCell(2).setCellValue(
                    device.getDeviceName() == null ? "" : device.getDeviceName());

            row.createCell(3).setCellValue(
                    device.getCategory() == null ? "" : device.getCategory());

            row.createCell(4).setCellValue(
                    device.getSerialNumber() == null ? "" : device.getSerialNumber());

            row.createCell(5).setCellValue(
                    device.getManufacturer() == null ? "" : device.getManufacturer());

            row.createCell(6).setCellValue(
                    device.getModelNumber() == null ? "" : device.getModelNumber());

            row.createCell(7).setCellValue(
                    device.getInvoiceNumber() == null ? "" : device.getInvoiceNumber());

            row.createCell(8).setCellValue(
                    device.getInvoiceDate() == null
                            ? ""
                            : device.getInvoiceDate().toString());

            row.createCell(9).setCellValue(
                    device.getAssetAge() == null ? 0 : device.getAssetAge());

            row.createCell(10).setCellValue(
                    device.getAssetPutUseDate() == null
                            ? ""
                            : device.getAssetPutUseDate().toString());

            row.createCell(11).setCellValue(
                    device.getWarrantyPeriod() == null
                            ? 0
                            : device.getWarrantyPeriod());

            row.createCell(12).setCellValue(
                    device.getWarrantyExpiryDate() == null
                            ? ""
                            : device.getWarrantyExpiryDate().toString());

            row.createCell(13).setCellValue(
                    device.getAssetLocation() == null
                            ? ""
                            : device.getAssetLocation());

            row.createCell(14).setCellValue(
                    device.getAssetCustodian() == null
                            ? ""
                            : device.getAssetCustodian());

            row.createCell(15).setCellValue(
                    device.getCost() == null ? 0.0 : device.getCost());

            row.createCell(16).setCellValue(
                    device.isAvailable() ? "Yes" : "No");

            row.createCell(17).setCellValue(
                    device.getEmployee() != null
                            ? device.getEmployee().getEmployeeName()
                            : "Not Assigned");

            row.createCell(18).setCellValue(
                    device.getEmployee() != null
                            ? device.getEmployee().getEmployeeId()
                            : "-");

            row.createCell(19).setCellValue(
                    device.getIssueDate() != null
                            ? device.getIssueDate().toString()
                            : "-");
        }

        // Auto-size all columns
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write workbook only once
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        outputStream.flush();
        workbook.close();
        outputStream.close();
    }
}