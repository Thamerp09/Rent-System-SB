package com.thamer.Rent_System.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thamer.Rent_System.model.*;
import com.thamer.Rent_System.repository.TenantRepository;
import com.thamer.Rent_System.repository.UploadedFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    // حقن المستودعات مباشرة لجلب البيانات
    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String processUserQuestion(String question) {
        try {
            // 1. تجهيز "السياق" (بيانات النظام كاملة)
            String systemContext = createSystemContext();

            // 2. تجهيز البرومبت
            // نطلب من Gemini أن يتصرف كمحاسب دقيق بناءً على البيانات المقدمة
            String prompt = String.format(
                    "أنت مساعد ذكي لنظام إدارة عقارات. لديك البيانات المالية التالية للنظام:\n" +
                            "------ بداية البيانات ------\n%s\n------ نهاية البيانات ------\n\n" +
                            "التعليمات:\n" +
                            "1. استخدم البيانات أعلاه فقط للإجابة.\n" +
                            "2. إذا سألك المستخدم عن مبالغ مستقبلية (مثلاً: خلال شهرين)، قم بجمع مبالغ الدفعات (RentRecord) التي حالتها 'NOT PAID' وتاريخ استحقاقها يقع ضمن الفترة المطلوبة بدقة.\n"
                            +
                            "3. أذكر اسم المستأجر وتاريخ الدفع عند التفصيل.\n" +
                            "4. السؤال هو: %s",
                    systemContext, question);

            // 3. إرسال الطلب
            return callGeminiApi(prompt);

        } catch (Exception e) {
            e.printStackTrace();
            return "حدث خطأ: " + e.getMessage();
        }
    }

    // --- هذا هو الجزء المأخوذ من SystemDumpService (الأفضل للدقة) ---
    @Transactional(readOnly = true)
    public String createSystemContext() {
        StringBuilder sb = new StringBuilder();
        sb.append("تاريخ النظام الحالي: ").append(LocalDateTime.now()).append("\n\n");

        List<Tenant> tenants = tenantRepository.findAll();

        for (Tenant tenant : tenants) {
            sb.append("المستأجر: ").append(tenant.getName()).append("\n");

            List<RentalContract> contracts = tenant.getRentalContract();
            if (contracts == null || contracts.isEmpty())
                continue;

            for (RentalContract contract : contracts) {
                sb.append(" - عقد رقم ").append(contract.getId())
                        .append(" (الموقع: ").append(contract.getLocation()).append(")\n");

                List<RentRecord> records = contract.getRentRecords();
                if (records != null) {
                    for (RentRecord record : records) {
                        // نرسل فقط الدفعات غير المدفوعة أو المستقبلية لتقليل حجم النص
                        // (يمكنك إزالة شرط !isPaid اذا أردت السجل كاملاً)
                        sb.append("   * دفعة مستحقة بتاريخ: ").append(record.getDueDate())
                                .append(" | المبلغ: ").append(record.getAmount())
                                .append(" | الحالة: ").append(record.isPaid() ? "مدفوعة" : "غير مدفوعة (NOT PAID)")
                                .append("\n");
                    }
                }
            }
            sb.append("----------------\n");
        }
        return sb.toString();
    }

    // --- دالة الاتصال بـ API ---
    private String callGeminiApi(String text) throws Exception {
        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode contents = requestBody.putArray("contents");
        ObjectNode partsNode = contents.addObject();
        ArrayNode parts = partsNode.putArray("parts");
        parts.addObject().put("text", text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String jsonString = requestBody.toString();
        HttpEntity<byte[]> request = new HttpEntity<>(jsonString.getBytes(StandardCharsets.UTF_8), headers);

        restTemplate.getMessageConverters().add(0,
                new org.springframework.http.converter.StringHttpMessageConverter(StandardCharsets.UTF_8));

        String finalUrl = apiUrl + apiKey;
        String response = restTemplate.postForObject(finalUrl, request, String.class);

        JsonNode root = objectMapper.readTree(response);
        return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
    }
}