# 🚀 SPRING BOOT PROFESSIONAL WEB API

[![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen?style=for-the-badge&logo=spring)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/JWT-Authentication-black?style=for-the-badge&logo=json-web-tokens)](https://jwt.io/)

Dự án này là một hệ thống Backend hoàn chỉnh được xây dựng với Java Spring Boot, tập trung vào kiến trúc sạch (Clean Architecture), bảo mật và khả năng mở rộng cao. Đây là minh chứng cho kỹ năng lập trình hướng đối tượng và tư duy thiết kế hệ thống của tôi.

---

## 📌 MỤC LỤC
1. [Tính năng chính](#-tính-năng-chính)
2. [Công nghệ sử dụng](#-công-nghệ-sử-dụng)
3. [Kiến trúc hệ thống](#-kiến-trúc-hệ-thống)
4. [Tài liệu API](#-tài-liệu-api-swagger)
5. [Hướng dẫn cài đặt](#-hướng-dẫn-cài-đặt)

---

## ✨ TÍNH NĂNG CHÍNH
* **Authentication & Authorization**: Hệ thống bảo mật 2 lớp sử dụng **Spring Security & JWT**. Phân quyền người dùng (Role-based: Admin/User).
* **RESTful APIs**: Cung cấp đầy đủ các phương thức CRUD chuẩn, sử dụng các HTTP Status Code hợp lý.
* **Global Exception Handling**: Cơ chế xử lý lỗi tập trung, giúp hệ thống luôn trả về thông báo lỗi nhất quán dưới dạng JSON.
* **Data Validation**: Kiểm soát dữ liệu đầu vào chặt chẽ từ phía server bằng **Hibernate Validator**.
* **Auto-mapping**: Sử dụng **MapStruct** hoặc **ModelMapper** để chuyển đổi giữa Entity và DTO, đảm bảo tính đóng gói của dữ liệu.

---

## 🛠 CÔNG NGHỆ SỬ DỤNG
* **Core**: Java 17, Spring Boot 3.x
* **Database**: MySQL/PostgreSQL với Spring Data JPA
* **Security**: Spring Security 6, JWT (JSON Web Token)
* **Tools**: Lombok, Maven, Docker, Swagger (OpenAPI 3.0)
* **Testing**: JUnit 5, Mockito

---

## 🏗 KIẾN TRÚC HỆ THỐNG
Dự án tuân thủ mô hình **Layered Architecture** giúp dễ dàng bảo trì và viết Unit Test:
* **Controller Layer**: Tiếp nhận và xử lý các Request từ Client.
* **Service Layer**: Chứa toàn bộ Logic nghiệp vụ của hệ thống.
* **Repository Layer**: Tương tác với Database thông qua Spring Data JPA.
* **Security Configuration**: Quản lý các Filter và cấu hình bảo mật.

---
