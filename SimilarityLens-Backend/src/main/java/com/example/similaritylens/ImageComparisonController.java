package com.example.similaritylens;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.similaritylens.model.ComparisonHistory;
import com.example.similaritylens.model.SharedFile;
import com.example.similaritylens.service.DatabaseService;

@RestController
@CrossOrigin(origins = "*")
public class ImageComparisonController {

    // Collaboration storage
    private final Map<String, String> waitingRooms = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> sharedImages = new ConcurrentHashMap<>();
    
    // Database Service
    @Autowired
    private DatabaseService databaseService;

    // ==================== HEALTH & TEST ENDPOINTS ====================
    
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "OK");
        status.put("message", "Backend is running!");
        return status;
    }


    // Add these test endpoints
@GetMapping("/db/test")
public Map<String, Object> testDatabase() {
    Map<String, Object> result = new HashMap<>();
    result.put("status", "OK");
    result.put("totalFiles", databaseService.getStatistics().get("totalFiles"));
    result.put("totalComparisons", databaseService.getStatistics().get("totalComparisons"));
    return result;
}

@PostMapping("/db/test-save")
public Map<String, String> testSave() {
    databaseService.saveSharedFile("test-room", "test-session", "left", 
        "test.jpg", "image", "test-data", null, 100);
    Map<String, String> response = new HashMap<>();
    response.put("status", "saved");
    return response;
}


@GetMapping("/db/test-insert")
public Map<String, String> testInsert() {
    Map<String, String> response = new HashMap<>();
    try {
        databaseService.saveSharedFile(
            "test-room-001", 
            "test-session-001", 
            "left", 
            "test.jpg", 
            "image", 
            "test-data-base64", 
            null, 
            100
        );
        response.put("status", "success");
        response.put("message", "Test data inserted successfully!");
        response.put("totalFiles", String.valueOf(databaseService.getStatistics().get("totalFiles")));
    } catch (Exception e) {
        response.put("status", "error");
        response.put("message", e.getMessage());
    }
    return response;
}


@GetMapping("/history/files")
public List<SharedFile> getAllFiles() {
    return databaseService.getAllFiles();
}

@GetMapping("/history/average")
public Map<String, Double> getAverageSimilarity() {
    Map<String, Double> response = new HashMap<>();
    response.put("average", databaseService.getAverageSimilarity());
    return response;
}

    @GetMapping("/")
    public Map<String, String> home() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "SimilarityLens is running!");
        response.put("ngrok", "Working properly");
        return response;
    }

    @GetMapping("/test")
    public String testPage() {
        return "<html><body><h1>SimilarityLens Test Page</h1><p>If you see this, the server is working!</p></body></html>";
    }

    // ==================== COMPARISON ENDPOINTS ====================
    
   @PostMapping("/compare/serial")
public Map<String, Object> compareSerial(@RequestParam("image1") MultipartFile file1,
                                          @RequestParam("image2") MultipartFile file2,
                                          @RequestParam(required = false) String roomId,
                                          @RequestParam(required = false) String sessionId) throws IOException {
    
    BufferedImage img1 = ImageIO.read(file1.getInputStream());
    BufferedImage img2 = ImageIO.read(file2.getInputStream());
    
    double similarity = calculateSimilarity(img1, img2);
    double percent = similarity * 100;
    
    // SAVE TO DATABASE - THIS WAS MISSING
    if (roomId != null && sessionId != null) {
        databaseService.saveComparisonResult(roomId, sessionId, null, "serial",
            file1.getOriginalFilename(), file2.getOriginalFilename(),
            similarity, percent, null);
        System.out.println("✅ Saved serial comparison to database: " + percent + "%");
    }
    
    Map<String, Object> response = new HashMap<>();
    response.put("ssim", String.format("%.4f", similarity));
    response.put("percent", String.format("%.2f", percent));
    return response;
}

    @PostMapping("/compare/multi")
public Map<String, Object> compareMulti(@RequestParam("image1") MultipartFile file1,
                                         @RequestParam("image2") MultipartFile file2,
                                         @RequestParam(required = false) String roomId,
                                         @RequestParam(required = false) String sessionId) throws IOException {
    
    BufferedImage img1 = ImageIO.read(file1.getInputStream());
    BufferedImage img2 = ImageIO.read(file2.getInputStream());
    
    double similarity = calculateSimilarity(img1, img2);
    double percent = similarity * 100;
    
    // SAVE TO DATABASE
    if (roomId != null && sessionId != null) {
        databaseService.saveComparisonResult(roomId, sessionId, null, "multi",
            file1.getOriginalFilename(), file2.getOriginalFilename(),
            similarity, percent, "3.5");
        System.out.println("✅ Saved multi comparison to database: " + percent + "%");
    }
    
    Map<String, Object> response = new HashMap<>();
    response.put("ssim", String.format("%.4f", similarity));
    response.put("percent", String.format("%.2f", percent));
    return response;
}

    @PostMapping("/compare/hybrid")
public Map<String, Object> compareText(@RequestBody Map<String, String> body,
                                        @RequestParam(required = false) String roomId,
                                        @RequestParam(required = false) String sessionId) {
    String text1 = body.get("text1");
    String text2 = body.get("text2");
    
    double sim = cosineSimilarity(text1, text2);
    double percent = sim * 100;
    
    // SAVE TO DATABASE
    if (roomId != null && sessionId != null) {
        databaseService.saveComparisonResult(roomId, sessionId, null, "hybrid",
            "text-file-1", "text-file-2", sim, percent, null);
        System.out.println("✅ Saved hybrid comparison to database: " + percent + "%");
    }
    
    Map<String, Object> response = new HashMap<>();
    response.put("cosine", String.format("%.4f", sim));
    response.put("percent", String.format("%.2f", percent));
    return response;
}

    @PostMapping("/compare/edge")
public Map<String, Object> compareGif(@RequestParam("gif1") MultipartFile file1,
                                       @RequestParam("gif2") MultipartFile file2,
                                       @RequestParam(required = false) String roomId,
                                       @RequestParam(required = false) String sessionId) throws IOException {
    List<BufferedImage> g1 = loadGif(file1);
    List<BufferedImage> g2 = loadGif(file2);
    double sim = compareGifSequential(g1, g2);
    double percent = sim * 100;
    
    // SAVE TO DATABASE
    if (roomId != null && sessionId != null) {
        databaseService.saveComparisonResult(roomId, sessionId, null, "edge",
            file1.getOriginalFilename(), file2.getOriginalFilename(),
            sim, percent, null);
        System.out.println("✅ Saved GIF sequential comparison to database: " + percent + "%");
    }
    
    Map<String, Object> response = new HashMap<>();
    response.put("gifSimilarity", String.format("%.4f", sim));
    response.put("percent", String.format("%.2f", percent));
    return response;
}
    
    @PostMapping("/compare/edge-parallel")
public Map<String, Object> compareGifParallelAPI(@RequestParam("gif1") MultipartFile file1,
                                                  @RequestParam("gif2") MultipartFile file2,
                                                  @RequestParam(required = false) String roomId,
                                                  @RequestParam(required = false) String sessionId) throws IOException {
    List<BufferedImage> g1 = loadGif(file1);
    List<BufferedImage> g2 = loadGif(file2);
    double sim = compareGifParallel(g1, g2);
    double percent = sim * 100;
    
    // SAVE TO DATABASE
    if (roomId != null && sessionId != null) {
        databaseService.saveComparisonResult(roomId, sessionId, null, "edge-parallel",
            file1.getOriginalFilename(), file2.getOriginalFilename(),
            sim, percent, null);
        System.out.println("✅ Saved GIF parallel comparison to database: " + percent + "%");
    }
    
    Map<String, Object> response = new HashMap<>();
    response.put("gifSimilarity", String.format("%.4f", sim));
    response.put("percent", String.format("%.2f", percent));
    return response;
}

    // ==================== CROSS-BROWSER COLLABORATION ENDPOINTS ====================
    
    @PostMapping("/collab/create")
    public Map<String, String> createSession() {
        String roomId = UUID.randomUUID().toString().substring(0, 8);
        waitingRooms.put(roomId, "waiting");
        Map<String, String> response = new HashMap<>();
        response.put("roomId", roomId);
        response.put("status", "created");
        System.out.println("📁 Room created: " + roomId);
        return response;
    }

    @PostMapping("/collab/join")
    public Map<String, String> joinSession(@RequestBody Map<String, String> request) {
        String roomId = request.get("roomId");
        Map<String, String> response = new HashMap<>();
        
        if (waitingRooms.containsKey(roomId) && "waiting".equals(waitingRooms.get(roomId))) {
            waitingRooms.put(roomId, "paired");
            response.put("status", "paired");
            response.put("message", "Successfully paired!");
            System.out.println("🔗 Room joined: " + roomId);
        } else if ("paired".equals(waitingRooms.get(roomId))) {
            response.put("status", "paired");
            response.put("message", "Already paired!");
        } else {
            response.put("status", "waiting");
            response.put("message", "Waiting for partner...");
        }
        return response;
    }

    @PostMapping("/collab/share")
    public Map<String, String> shareImage(@RequestBody Map<String, String> request) {
        String roomId = request.get("roomId");
        String side = request.get("side");
        String imageData = request.get("imageData");
        String fileType = request.getOrDefault("fileType", "image");
        String textContent = request.get("textContent");
        String panel = request.getOrDefault("panel", "serial");
        String filename = request.getOrDefault("filename", side + "_file");
        String sessionId = request.getOrDefault("sessionId", "unknown");
        
        // Get or create room storage
        Map<String, Object> roomStorage = sharedImages.get(roomId);
        if (roomStorage == null) {
            roomStorage = new ConcurrentHashMap<>();
            sharedImages.put(roomId, roomStorage);
        }
        
        // Store with panel information
        String key = side + "_image";
        roomStorage.put(key, imageData);
        roomStorage.put(key + "_timestamp", System.currentTimeMillis());
        roomStorage.put(key + "_type", fileType);
        roomStorage.put(key + "_panel", panel);
        
        // For text files, store the actual text content
        if (fileType.equals("text") && textContent != null) {
            roomStorage.put(side + "_text", textContent);
            System.out.println("📝 Text stored for room " + roomId + ", side " + side);
        }
        
        // Save to database
        int fileSize = imageData != null ? imageData.length() : 0;
        databaseService.saveSharedFile(roomId, sessionId, side, filename, fileType, imageData, textContent, fileSize);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "shared");
        response.put("message", "Image shared successfully");
        response.put("side", side);
        response.put("panel", panel);
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        System.out.println("📸 Image shared - Room: " + roomId + ", Side: " + side + ", Panel: " + panel + ", Type: " + fileType);
        return response;
    }

    @GetMapping("/collab/get/{roomId}")
    public Map<String, Object> getSharedData(@PathVariable String roomId) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> roomData = sharedImages.get(roomId);
        
        if (roomData != null) {
            Map<String, Object> images = new HashMap<>();
            
            // Get left image data with panel info
            if (roomData.containsKey("left_image")) {
                images.put("left_image", roomData.get("left_image"));
                if (roomData.containsKey("left_text")) {
                    images.put("left_text", roomData.get("left_text"));
                }
                if (roomData.containsKey("left_image_timestamp")) {
                    images.put("left_timestamp", roomData.get("left_image_timestamp"));
                }
                if (roomData.containsKey("left_image_panel")) {
                    images.put("left_panel", roomData.get("left_image_panel"));
                }
            }
            
            // Get right image data with panel info
            if (roomData.containsKey("right_image")) {
                images.put("right_image", roomData.get("right_image"));
                if (roomData.containsKey("right_text")) {
                    images.put("right_text", roomData.get("right_text"));
                }
                if (roomData.containsKey("right_image_timestamp")) {
                    images.put("right_timestamp", roomData.get("right_image_timestamp"));
                }
                if (roomData.containsKey("right_image_panel")) {
                    images.put("right_panel", roomData.get("right_image_panel"));
                }
            }
            
            response.put("images", images);
        } else {
            response.put("images", new HashMap<>());
        }
        
        response.put("hasPartner", "paired".equals(waitingRooms.get(roomId)));
        response.put("roomExists", waitingRooms.containsKey(roomId));
        return response;
    }
    
    @PostMapping("/collab/clear/{roomId}")
    public Map<String, String> clearRoom(@PathVariable String roomId) {
        waitingRooms.remove(roomId);
        sharedImages.remove(roomId);
        Map<String, String> response = new HashMap<>();
        response.put("status", "cleared");
        System.out.println("🗑️ Room cleared: " + roomId);
        return response;
    }

    // ==================== HISTORY ENDPOINTS ====================
    
    @GetMapping("/history/room/{roomId}")
    public Map<String, Object> getRoomHistory(@PathVariable String roomId) {
        Map<String, Object> response = new HashMap<>();
        response.put("comparisons", databaseService.getRoomHistory(roomId));
        response.put("files", sharedImages.getOrDefault(roomId, new HashMap<>()));
        return response;
    }

    @GetMapping("/history/recent")
    public List<ComparisonHistory> getRecentComparisons() {
        return databaseService.getRecentComparisons();
    }
    
    @GetMapping("/history/stats")
    public Map<String, Object> getStatistics() {
        return databaseService.getStatistics();
    }

    // ==================== HELPER METHODS ====================
    
    static double cosineSimilarity(String t1, String t2) {
        if (t1 == null || t2 == null) return 0;
        if (t1.isEmpty() || t2.isEmpty()) return 0;
        if (t1.equals(t2)) return 1.0;
        
        java.util.Map<String, Integer> f1 = getFreq(t1);
        java.util.Map<String, Integer> f2 = getFreq(t2);

        java.util.Set<String> all = new java.util.HashSet<>();
        all.addAll(f1.keySet());
        all.addAll(f2.keySet());

        double dot = 0;
        double m1 = 0;
        double m2 = 0;

        for (String w : all) {
            int a = f1.getOrDefault(w, 0);
            int b = f2.getOrDefault(w, 0);
            dot += a * b;
            m1 += a * a;
            m2 += b * b;
        }
        if (m1 == 0 || m2 == 0) return 0;
        return dot / (Math.sqrt(m1) * Math.sqrt(m2));
    }

    static java.util.Map<String, Integer> getFreq(String text) {
        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        text = text.toLowerCase().replaceAll("[^a-z ]", "");
        for (String w : text.split("\\s+")) {
            if (!w.isEmpty()) {
                map.put(w, map.getOrDefault(w, 0) + 1);
            }
        }
        return map;
    }
    
    static double compareGifSequential(List<BufferedImage> a, List<BufferedImage> b) {
        int n = Math.min(a.size(), b.size());
        if (n == 0) return 0;
        double total = 0;
        for (int i = 0; i < n; i++) {
            total += calculateSimilarity(a.get(i), b.get(i));
        }
        return total / n;
    }

    static List<BufferedImage> loadGif(MultipartFile file) throws IOException {
        ImageInputStream stream = ImageIO.createImageInputStream(file.getInputStream());
        Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
        if (!readers.hasNext()) return new ArrayList<>();
        ImageReader reader = readers.next();
        reader.setInput(stream);
        List<BufferedImage> frames = new ArrayList<>();
        int count = reader.getNumImages(true);
        for (int i = 0; i < count; i++) {
            frames.add(reader.read(i));
        }
        reader.dispose();
        stream.close();
        return frames;
    }

    @SuppressWarnings("UseSpecificCatch")
    static double compareGifParallel(java.util.List<BufferedImage> a, java.util.List<BufferedImage> b) {
        int n = Math.min(a.size(), b.size());
        if (n == 0) return 0;
        int threads = 4;
        GifTask[] tasks = new GifTask[threads];
        Thread[] t = new Thread[threads];
        int step = n / threads;
        for (int i = 0; i < threads; i++) {
            int start = i * step;
            int end = (i == threads - 1) ? n : start + step;
            tasks[i] = new GifTask(a, b, start, end);
            t[i] = new Thread(tasks[i]);
            t[i].start();
        }
        double total = 0;
        for (int i = 0; i < threads; i++) {
            try { t[i].join(); } catch (Exception ignored) {}
            total += tasks[i].sum;
        }
        return total / n;
    }
    
    static class GifTask implements Runnable {
        java.util.List<BufferedImage> a, b;
        int start, end;
        double sum = 0;
        GifTask(java.util.List<BufferedImage> a, java.util.List<BufferedImage> b, int start, int end) {
            this.a = a; this.b = b; this.start = start; this.end = end;
        }
        @SuppressWarnings("override")
        public void run() {
            for (int i = start; i < end; i++) {
                sum += calculateSimilarity(a.get(i), b.get(i));
            }
        }
    }
    
    private static double calculateSimilarity(BufferedImage img1, BufferedImage img2) {
        int width = Math.min(img1.getWidth(), img2.getWidth());
        int height = Math.min(img1.getHeight(), img2.getHeight());
        
        long totalDiff = 0;
        int pixels = width * height;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);
                
                int r1 = (rgb1 >> 16) & 0xFF;
                int g1 = (rgb1 >> 8) & 0xFF;
                int b1 = rgb1 & 0xFF;
                
                int r2 = (rgb2 >> 16) & 0xFF;
                int g2 = (rgb2 >> 8) & 0xFF;
                int b2 = rgb2 & 0xFF;
                
                int diff = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
                totalDiff += diff;
            }
        }
        
        double maxPossibleDiff = pixels * 3 * 255;
        double similarity = 1.0 - (totalDiff / maxPossibleDiff);
        return Math.max(0, Math.min(1, similarity));
    }
}