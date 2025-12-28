package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.service.TariffReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/tariff-reports")
public class TariffReportController {

    @Autowired
    private TariffReportService tariffReportService;

    /**
     * Export all active tariffs to PDF
     * GET /api/tariff-reports/export-pdf
     */
    @GetMapping("/export-pdf")
    public void exportTariffPDF(HttpServletRequest request, HttpServletResponse response) {
        try {
            System.out.println("=== Export Tariff PDF Endpoint Called ===");

            // Debug: Print all request headers
            System.out.println("Request Origin: " + request.getHeader("Origin"));
            System.out.println("Request Method: " + request.getMethod());

            // Add explicit CORS headers IMMEDIATELY (needed when writing directly to HttpServletResponse)
            String origin = request.getHeader("Origin");
            System.out.println("DEBUG: Origin header value: " + origin);

            if (origin != null && (origin.equals("http://localhost:3000") ||
                                   origin.equals("http://localhost:8095") ||
                                   origin.equals("http://127.0.0.1:3000"))) {
                System.out.println("DEBUG: Setting CORS headers for origin: " + origin);
                response.setHeader("Access-Control-Allow-Origin", origin);
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
                response.setHeader("Access-Control-Expose-Headers", "Content-Disposition, Content-Type, Content-Length");
                System.out.println("DEBUG: CORS headers set successfully");
            } else {
                System.out.println("DEBUG: Origin not matched or null. Origin = " + origin);
            }

            tariffReportService.generateTariffPDF(response);
            System.out.println("=== PDF Export Successful ===");
        } catch (Exception e) {
            System.err.println("=== ERROR: PDF Export Failed ===");
            e.printStackTrace();
            try {
                // Add CORS headers to error response as well
                String origin = request.getHeader("Origin");
                if (origin != null && (origin.equals("http://localhost:3000") ||
                                       origin.equals("http://localhost:8095") ||
                                       origin.equals("http://127.0.0.1:3000"))) {
                    response.setHeader("Access-Control-Allow-Origin", origin);
                    response.setHeader("Access-Control-Allow-Credentials", "true");
                    System.out.println("DEBUG: CORS headers set in error handler");
                }

                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Failed to generate PDF: " + e.getMessage() + "\"}");
            } catch (Exception writeError) {
                writeError.printStackTrace();
            }
        }
    }

    /**
     * Handle OPTIONS preflight request for CORS
     */
    @RequestMapping(value = "/export-pdf", method = RequestMethod.OPTIONS)
    public void handlePreflight(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("=== OPTIONS Preflight Request Received ===");
        String origin = request.getHeader("Origin");
        System.out.println("DEBUG OPTIONS: Origin = " + origin);

        if (origin != null && (origin.equals("http://localhost:3000") ||
                               origin.equals("http://localhost:8095") ||
                               origin.equals("http://127.0.0.1:3000"))) {
            System.out.println("DEBUG OPTIONS: Setting CORS headers for origin: " + origin);
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
            response.setHeader("Access-Control-Max-Age", "3600");
            System.out.println("DEBUG OPTIONS: CORS headers set successfully");
        } else {
            System.out.println("DEBUG OPTIONS: Origin not matched or null");
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Test endpoint to verify controller is working
     */
    @GetMapping("/test")
    public String test() {
        return "Tariff Report API is working";
    }
}
