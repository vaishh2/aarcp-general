package com.recipeai.recipebackend.service;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.documentai.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.util.*;

@Service
public class DocumentAIService {

    @Value("${documentai.project-id}")
    private String projectId;

    @Value("${documentai.location}")
    private String location;

    @Value("${documentai.processor-id}")
    private String processorId;

    @Value("${documentai.credentials}")
    private String credentialsPath;

    // Fixed headers in Excel (order preserved)
    private static final List<String> FIXED_HEADERS = Arrays.asList(
            "Invoice Number", "Invoice Date", "Customer Name", "Customer Number",
            "Line No", "Service Description", "Quantity", "Unit Price", "Line Amount", "Invoice Amount"
    );

    public static class ExtractionResult {
        public final List<String> headers = new ArrayList<>(FIXED_HEADERS);
        public final List<Map<String, String>> rows = new ArrayList<>();
        public String sheetName;

        // New metadata fields (public so ExcelService can read them)
        public String invoiceNumber;
        public String invoiceDate;
        public String customerName;
        public String customerNumber;
        public String invoiceAmount;
    }

    public List<ExtractionResult> processDocument(byte[] fileBytes) throws Exception {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath))
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));

        DocumentProcessorServiceSettings settings = DocumentProcessorServiceSettings.newBuilder()
                .setEndpoint(location + "-documentai.googleapis.com:443")
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();

        List<ExtractionResult> results = new ArrayList<>();
        try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create(settings)) {
            String name = String.format("projects/%s/locations/%s/processors/%s", projectId, location, processorId);

            RawDocument raw = RawDocument.newBuilder()
                    .setContent(ByteString.copyFrom(fileBytes))
                    .setMimeType("application/pdf")
                    .build();

            ProcessRequest request = ProcessRequest.newBuilder()
                    .setName(name)
                    .setRawDocument(raw)
                    .build();

            ProcessResponse response = client.processDocument(request);
            Document doc = response.getDocument();

            int pageCount = doc.getPagesCount();
            ExtractionResult currentSheet = null;
            List<String> lastSchema = null;

            for (int p = 0; p < pageCount; p++) {
                // ---- Extract raw entity values (only actual entities, not placeholders)
                String invoiceNumberEntity = firstNonNull(
                        findEntityOnPage(doc, "invoice_id", p),
                        findEntityOnPage(doc, "invoice_number", p)
                );

                String invoiceDateEntity = findEntityOnPage(doc, "invoice_date", p);

                String customerNameEntity = firstNonNull(
                        findEntityOnPage(doc, "customer_name", p),
                        findEntityOnPage(doc, "bill_to_name", p),
                        findEntityOnPage(doc, "ship_to_name", p),
                        findEntityOnPage(doc, "supplier_name", p)
                );

                String customerNumEntity = firstNonNull(
                        findEntityOnPage(doc, "customer_id", p),
                        findEntityOnPage(doc, "customer_number", p)
                );

                String invoiceTotalEntity = firstNonNull(
                        findEntityOnPage(doc, "total_amount", p),
                        findEntityOnPage(doc, "net_amount", p)
                );

                // ---- For naming only: fall back to a placeholder if no entity exists
                String invoiceNameForSheet = (invoiceNumberEntity != null && !invoiceNumberEntity.isBlank())
                        ? invoiceNumberEntity : "Invoice_" + (p + 1);

                // ---- detect schema of this page
                List<String> currentSchema = extractTableHeaders(doc, p);

                // Determine if this page should start a new sheet:
                boolean hasMetadata = invoiceNumberEntity != null || invoiceDateEntity != null
                        || customerNameEntity != null || customerNumEntity != null;
                boolean isNewSheet = false;
                if (hasMetadata) {
                    // Case 1: metadata present -> new sheet
                    isNewSheet = true;
                } else if (lastSchema != null && !schemasMatch(lastSchema, currentSchema)) {
                    // Case 3: schema mismatch -> new sheet
                    isNewSheet = true;
                }
                // else Case 2: continuation -> append to currentSheet

                if (isNewSheet || currentSheet == null) {
                    currentSheet = new ExtractionResult();
                    currentSheet.sheetName = sanitizeSheetName(invoiceNameForSheet);

                    // Only populate metadata fields with actual entity values (null if absent).
                    currentSheet.invoiceNumber  = invoiceNumberEntity;
                    currentSheet.invoiceDate    = invoiceDateEntity;
                    currentSheet.customerName   = customerNameEntity;
                    currentSheet.customerNumber = customerNumEntity;
                    currentSheet.invoiceAmount  = invoiceTotalEntity;

                    results.add(currentSheet);
                } else {
                    // continuation page: do NOT overwrite metadata fields on currentSheet.
                    // (currentSheet keeps metadata set when it was created)
                }

                lastSchema = currentSchema;

                // ---- Line items on this page (unchanged)
                List<Document.Entity> lineItems = entitiesOfTypeOnPage(doc, "line_item", p);
                int lineNo = currentSheet.rows.size() + 1;

                for (Document.Entity li : lineItems) {
                    String desc       = childValue(li, "line_item/description");
                    String qty        = firstNonNull(childValue(li, "line_item/quantity"),
                                                     childValue(li, "line_item/qty"));
                    String unitPrice  = firstNonNull(childValue(li, "line_item/unit_price"),
                                                     childValue(li, "line_item/unit_price_base"));
                    String amount     = firstNonNull(childValue(li, "line_item/amount"),
                                                     childValue(li, "line_item/line_extension"));

                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("Invoice Number", nullToEmpty(currentSheet.invoiceNumber));
                    row.put("Invoice Date",   nullToEmpty(currentSheet.invoiceDate));
                    row.put("Customer Name",  nullToEmpty(currentSheet.customerName));
                    row.put("Customer Number",nullToEmpty(currentSheet.customerNumber));
                    row.put("Line No",        String.valueOf(lineNo++));
                    row.put("Service Description", nullToEmpty(desc));
                    row.put("Quantity",       nullToEmpty(qty));
                    row.put("Unit Price",     nullToEmpty(unitPrice));
                    row.put("Line Amount",    nullToEmpty(amount));
                    row.put("Invoice Amount", nullToEmpty(currentSheet.invoiceAmount));

                    currentSheet.rows.add(row);
                }

                // If no line items were detected on this page, still create a placeholder row
                if (lineItems.isEmpty()) {
                    Map<String, String> row = new LinkedHashMap<>();
                    row.put("Invoice Number", nullToEmpty(currentSheet.invoiceNumber));
                    row.put("Invoice Date",   nullToEmpty(currentSheet.invoiceDate));
                    row.put("Customer Name",  nullToEmpty(currentSheet.customerName));
                    row.put("Customer Number",nullToEmpty(currentSheet.customerNumber));
                    row.put("Line No",        "1");
                    row.put("Service Description", "");
                    row.put("Quantity", "");
                    row.put("Unit Price", "");
                    row.put("Line Amount", "");
                    row.put("Invoice Amount", nullToEmpty(currentSheet.invoiceAmount));
                    currentSheet.rows.add(row);
                }
            }
        }

        return results;
    }

    // ------- helpers

    private static String firstNonNull(String... vals) {
        for (String v : vals) if (v != null && !v.isBlank()) return v;
        return null;
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }

    /** Find the value of an entity type on a specific page (using page anchors). */
    private static String findEntityOnPage(Document doc, String type, int pageIndex) {
        for (Document.Entity e : doc.getEntitiesList()) {
            if (!type.equalsIgnoreCase(e.getType())) continue;
            if (!e.hasPageAnchor()) continue;
            for (Document.PageAnchor.PageRef ref : e.getPageAnchor().getPageRefsList()) {
                if (ref.getPage() == pageIndex) {
                    return safeText(e);
                }
            }
        }
        return null;
    }

    /** Collect all entities of a type that appear on a given page. */
    private static List<Document.Entity> entitiesOfTypeOnPage(Document doc, String type, int pageIndex) {
        List<Document.Entity> out = new ArrayList<>();
        for (Document.Entity e : doc.getEntitiesList()) {
            if (!type.equalsIgnoreCase(e.getType())) continue;
            if (!e.hasPageAnchor()) continue;
            for (Document.PageAnchor.PageRef ref : e.getPageAnchor().getPageRefsList()) {
                if (ref.getPage() == pageIndex) {
                    out.add(e);
                    break;
                }
            }
        }
        return out;
    }

    /** Read a child property value (Invoice Parser nests line_item fields as child entities). */
    private static String childValue(Document.Entity parent, String childType) {
        for (Document.Entity c : parent.getPropertiesList()) {
            if (childType.equalsIgnoreCase(c.getType())) {
                return safeText(c);
            }
        }
        return null;
    }

    private static String safeText(Document.Entity e) {
        String t = e.getMentionText();
        return t == null ? "" : t.trim();
    }

    /** Excel sheet name rules: <=31 chars, no []:*?/\, not empty; dedupe handled in ExcelService. */
    private static String sanitizeSheetName(String name) {
        if (name == null || name.isBlank()) name = "Invoice";
        String cleaned = name.replaceAll("[\\\\/*?:\\[\\]]", "_");
        if (cleaned.length() > 31) cleaned = cleaned.substring(0, 31);
        if (cleaned.isBlank()) cleaned = "Invoice";
        return cleaned;
    }

    // --- schema helpers
    private static List<String> extractTableHeaders(Document doc, int pageIndex) {
        List<String> headers = new ArrayList<>();
        if (doc.getPagesCount() > pageIndex) {
            for (Document.Page.Table t : doc.getPages(pageIndex).getTablesList()) {
                if (!t.getHeaderRowsList().isEmpty()) {
                    for (Document.Page.Table.TableRow row : t.getHeaderRowsList()) {
                        for (Document.Page.Table.TableCell cell : row.getCellsList()) {
                            String text = "";
                            try {
                                if (cell.hasLayout() && cell.getLayout().hasTextAnchor()
                                        && cell.getLayout().getTextAnchor().getContent() != null) {
                                    text = cell.getLayout().getTextAnchor().getContent().trim();
                                }
                            } catch (Exception ex) {
                                // be defensive: ignore header cell extraction problems
                            }
                            if (!text.isEmpty()) headers.add(text);
                        }
                    }
                }
            }
        }
        return headers;
    }

    private static boolean schemasMatch(List<String> s1, List<String> s2) {
        if (s1 == null || s2 == null) return false;
        if (s1.size() != s2.size()) return false;
        for (int i = 0; i < s1.size(); i++) {
            if (!s1.get(i).equalsIgnoreCase(s2.get(i))) return false;
        }
        return true;
    }
}
