🎯 What is SimilarityLens?
SimilarityLens is a visual intelligence platform that compares images, GIFs, and text documents using multiple algorithms. It features both single-threaded and multi-threaded processing for optimal performance, plus real-time collaboration capabilities.

⚡ What It Does
Compare Images using SSIM (Structural Similarity Index)

Analyze GIFs frame-by-frame (sequential & parallel)

Compare Text Documents using Cosine Similarity

Real-time Collaboration - Share files with partners

History Tracking - View all past comparisons

🔬 How It Works
Algorithms Used:
SSIM (Structural Similarity)

Compares brightness, contrast, and structure

Mimics human visual perception

Multi-Thread Processing

Splits images into chunks

Processes simultaneously for speed

Perceptual Hashing

Generates image fingerprints

Detects duplicates & near-duplicates

Edge Detection

Analyzes image gradients

Compares structural boundaries

Cosine Similarity

Compares text documents

Uses word frequency analysis

🚀 Quick Start
bash
# 1. Start MySQL (XAMPP)
# 2. Create database
CREATE DATABASE similaritylens;

# 3. Run the application
mvn spring-boot:run

# 4. Open browser
http://localhost:8080/lab.html

🖼️ Screenshots
Home Page
<img width="1920" height="839" alt="image" src="https://github.com/user-attachments/assets/6f18c4b3-be1a-4ae9-8563-9b564d6cfeb2" />


Lab Page - Serial SSIM
<img width="941" height="407" alt="image" src="https://github.com/user-attachments/assets/1e85d5a1-b3ca-4477-824c-9fa6923be6f5" />


Lab Page - Multi-Thread SSIM
<img width="928" height="412" alt="image" src="https://github.com/user-attachments/assets/565e353b-ed02-4702-8c3b-17015036b8a5" />


Text Similarity
<img width="925" height="404" alt="image" src="https://github.com/user-attachments/assets/0bbd8dc3-fa33-43b9-a36f-15c0649959c4" />


GIF Comparison
<img width="578" height="419" alt="image" src="https://github.com/user-attachments/assets/9512fcd6-3a1c-4ef5-a3c7-319107f56044" />


Collaboration Feature
<img width="328" height="41" alt="image" src="https://github.com/user-attachments/assets/2889746d-fbab-4138-90b7-58d99a34bff4" />


Blog page
<img width="895" height="412" alt="image" src="https://github.com/user-attachments/assets/e9b2a746-fe03-41d6-9e9a-5174cbf5a742" />

Xampp
<img width="572" height="78" alt="image" src="https://github.com/user-attachments/assets/dd3f81e4-9a89-4eb9-b097-e7bc0b1cbcc7" />


🛠️ Tech Stack
Backend: Java 17, Spring Boot 2.7.0, Hibernate

Database: MySQL

Frontend: HTML5, CSS3, JavaScript

Communication: WebSocket (STOMP)
