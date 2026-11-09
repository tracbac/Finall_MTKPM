# HỆ THỐNG QUẢN LÝ THƯ VIỆN SỐ (DIGITAL LIBRARY MANAGEMENT SYSTEM)

> **Báo cáo Tiểu luận Kết thúc Học phần: Mẫu Thiết Kế Phần Mềm (Software Design Patterns)**  
> **Nền tảng công nghệ:** Java 21, Spring Boot 3.3.2, Spring Security, Spring Data JPA, JWT, H2 Database / Supabase, HTML5/CSS3/JavaScript (SPA).  
> **Bộ kiểm thử tự động:** 112/112 Unit & Integration Tests đạt chuẩn (`BUILD SUCCESS`).

---

## 1. Giới thiệu Đề tài

Hệ thống Quản lý Thư viện Số là giải pháp phần mềm toàn diện hỗ trợ thư viện trường đại học chuyển đổi số:
- **Quản lý đa dạng tài liệu:** Hỗ trợ song song Sách in truyền thống (Physical Book), Sách điện tử (E-Book) và Sách nói (Audio Book).
- **Mượn/Trả & Quản lý phạt quá hạn:** Xử lý quy trình mượn sách tại chỗ, tính toán tiền phạt quá hạn theo ngày, lập lịch quét tự động nhắc hạn và khóa tài khoản khi có nợ phạt.
- **Bảo vệ bản quyền số (DRM - Digital Rights Management):** Cấp giấy phép tải tệp tài liệu điện tử theo thời hạn và hạn mức tải, tích hợp đóng dấu bản quyền cá nhân (Watermarking).
- **Tích hợp kho sách toàn cầu:** Kết nối trực tuyến với Google Books API để tìm kiếm, tra cứu và nhập kho sách tự động.
- **Hệ thống thông báo tức thời:** Tự động thông báo trạng thái mượn/trả, phê duyệt giấy phép và nhắc hạn mượn cho bạn đọc và thủ thư.

---

## 2. Bảng Ánh xạ Mẫu Thiết Kế (GoF Design Patterns)

Hệ thống được thiết kế theo chuẩn hướng đối tượng hiện đại, áp dụng chặt chẽ các mẫu thiết kế GoF và nguyên tắc SOLID:

| STT | Mẫu Thiết Kế | Phân Loại | Vị Trí Triển Khai (Source Code) | Vai Trò & Nghiệp Vụ Cụ Thể |
| :---: | :--- | :---: | :--- | :--- |
| **1** | **Factory Method** | *Creational* | `service2.factory.*`<br>`LibraryItemCreator2.java`<br>`EBookCreator2.java`<br>`PhysicalBookCreator2.java`<br>`AudioBookCreator2.java` | Đóng gói logic khởi tạo các dòng sản phẩm tài liệu khác nhau (`EBookItem2`, `PhysicalBookItem2`, `AudioBookItem2`) mà không làm lộ chi tiết phụ thuộc ở tầng dịch vụ. |
| **2** | **Template Method** | *Behavioral* | `LibraryItemCreator2.java`<br>(phương thức `buildValidatedItem()`) | Định nghĩa bộ khung thuật toán thẩm định và chuẩn hóa dữ liệu cố định (`final`), sau đó gọi phương thức trừu tượng `createItem()` để từng Creator con quyết định kiểu thể hiện cụ thể. |
| **3** | **Strategy** | *Behavioral* | `service2.strategy.*`<br>`BookSearchStrategy2.java`<br>`TitleSearchStrategy2.java`<br>`AuthorSearchStrategy2.java`<br>`CategorySearchStrategy2.java`<br>`AdvancedCombinedSearchStrategy2.java`<br>`BookSearchContext2.java` | Cho phép độc giả và thủ thư chuyển đổi linh hoạt các thuật toán tra cứu sách (theo tiêu đề, tác giả, danh mục thể loại hoặc tìm kiếm kết hợp đa tiêu chí) tại thời điểm thực thi (*runtime*). |
| **4** | **Adapter** | *Structural* | `service2.adapter.*`<br>`ExternalBookCatalogPort2.java`<br>`GoogleBooksAdapter2.java`<br>`GoogleBooksRestClient2.java`<br>`ExternalBookDto2.java` | Chuyển đổi giao diện dữ liệu phức tạp từ Google Books API bên ngoài về chuẩn giao tiếp `ExternalBookCatalogPort2` nội bộ, giúp ứng dụng không bị phụ thuộc vào API của bên thứ ba. |
| **5** | **Proxy** | *Structural* | `service3.EBookDownloadProxy3.java`<br>`service3.RealEBookDownloadService3.java` | Hoạt động như một **Protection Proxy** bảo vệ tệp PDF bản quyền. Kiểm tra nghiêm ngặt 7 tầng điều kiện (xác thực người dùng, nợ phạt quá hạn, tính hợp lệ của giấy phép DRM, hạn mức tải) trước khi ủy quyền cho Service tải tệp thật. |
| **6** | **Observer** | *Behavioral* | `service4.observer.*`<br>`LibraryEvent4.java`<br>`InAppNotificationListener4.java`<br>`NotificationAuditListener4.java` | Phân tách (*decouple*) hoàn toàn luồng nghiệp vụ mượn/trả khỏi cơ chế thông báo. Khi có sự kiện (gửi yêu cầu, duyệt mượn, cấp DRM, trả sách, quét trễ hạn), Subject tự động phát sự kiện tới các Observer ghi nhận thông báo độc giả và log kiểm toán. |
| **7** | **Builder** | *Creational* | `model3.DrmLicense3.java`<br>`model3.LoanRecord3.java`<br>`model3.DownloadLog3.java` | Xây dựng các đối tượng nghiệp vụ phức tạp có nhiều thông số một cách trực quan, tránh lỗi thứ tự tham số constructor. |

---

## 3. Áp dụng Nguyên tắc Thiết kế Hướng đối tượng (SOLID)

- **SRP (Single Responsibility):** Mỗi lớp chỉ đảm nhận một trách nhiệm duy nhất (ví dụ: `GoogleBooksRestClient2` chỉ giao tiếp HTTP, `GoogleBooksAdapter2` chỉ chuyển đổi DTO, `BookCatalogService2` phụ trách nghiệp vụ danh mục).
- **OCP (Open/Closed):** Dễ dàng mở rộng thêm loại tài liệu mới (vd: Tạp chí - `JournalItem2`) bằng cách kế thừa `LibraryItemCreator2` mà không cần chỉnh sửa mã nguồn hiện có.
- **LSP (Liskov Substitution):** Tất cả các lớp con của `LibraryItem2` đều có thể thay thế hoàn toàn cho lớp cha mà không làm thay đổi tính đúng đắn của chương trình, không có phương thức nào ném `UnsupportedOperationException`.
- **ISP (Interface Segregation):** Tách nhỏ các hành vi chuyên biệt thay vì dùng interface "béo": `DigitalDownloadable2` (chỉ dành cho sách tải số), `PhysicalShelvable2` (sách giấy tại kệ), `AudioStreamable2` (sách nói nghe trực tuyến).
- **DIP (Dependency Inversion):** Các Service phụ thuộc vào Interface trừu tượng (như `ExternalBookCatalogPort2`, `BookCoverStorage`), không phụ thuộc trực tiếp vào tầng triển khai cụ thể (`GoogleBooksAdapter2`, `SupabaseBookCoverStorage`).

---

## 4. Hướng dẫn Cài đặt & Khởi chạy Demo

Dự án được cấu hình sẵn chế độ lưu trữ **H2 Database (In-Memory / File)** và bộ sinh dữ liệu tự động (`DataSeeder2.java`), giúp thầy cô và người chấm có thể khởi chạy và trải nghiệm ngay lập tức mà không cần cài đặt cơ sở dữ liệu ngoài.

### 4.1. Yêu cầu Môi trường
- **Java Development Kit (JDK):** Phiên bản **21** trở lên (hỗ trợ kiểm thử trên JDK 21 và JDK 25).
- Không yêu cầu cài đặt Maven (dự án đã tích hợp sẵn Maven Wrapper).

### 4.2. Các bước Khởi chạy Ứng dụng
1. Mở cửa sổ dòng lệnh (Terminal / PowerShell / Command Prompt) tại thư mục gốc của dự án.
2. Khởi chạy ứng dụng:
   ```bash
   # Trên hệ điều hành Windows:
   .\mvnw.cmd spring-boot:run

   # Trên macOS / Linux:
   ./mvnw spring-boot:run
   ```
3. Sau khi ứng dụng khởi động thành công (thông báo `Started DigitalLibraryApplication in ... seconds`), mở trình duyệt web và truy cập địa chỉ:
   ```
   http://localhost:8080
   ```

### 4.3. Chạy Toàn bộ Kiểm thử Tự động (Automated Test Suite)
Để kiểm tra độ tin cậy và sự ổn định của 112 bài kiểm thử (Bao gồm Unit Tests và Security Integration Tests):
```bash
# Windows:
.\mvnw.cmd test

# macOS / Linux:
./mvnw test
```
*Kết quả mong đợi:* **`BUILD SUCCESS`** (112/112 tests pass, 0 failures, 0 errors).

---

## 5. Tài khoản Trải nghiệm Hệ thống (Tạo sẵn)

Hệ thống đã tự động nạp dữ liệu mẫu ban đầu:

| Loại Tài Khoản | Tên Đăng Nhập | Mật Khẩu | Quyền Hạn & Các Chức Năng Chính |
| :--- | :--- | :--- | :--- |
| **Quản trị viên (Admin)** | `admin` | `admin123` | Quản lý kho sách, tra cứu & nhập sách từ Google Books, duyệt mượn/trả sách, duyệt cấp phép E-Book DRM, quản lý tài khoản người dùng và xem log thông báo. |
| **Độc giả (Reader)** | `reader` | `reader123` | Tìm kiếm & lọc sách (theo Strategy), gửi yêu cầu mượn sách giấy, mượn E-Book DRM, gia hạn thời gian mượn, tải tệp tài liệu số có watermark và nhận thông báo. |

---

## 6. Danh mục Tài liệu và Minh chứng Đính kèm

- **Báo cáo Tiểu luận Chính thức:** File [`Tieu_luan_MTKPM_He_thong_quan_ly_thu_vien_so.docx`](./Tieu_luan_MTKPM_He_thong_quan_ly_thu_vien_so.docx) (68 trang hoàn chỉnh, chứa đầy đủ mục lục tự động, 28 hình vẽ sơ đồ thiết kế UML và ảnh chụp kết quả demo thực tế, bảng phân công nhiệm vụ thành viên).
- **Thư mục Ảnh Minh chứng Demo:** Thư mục [`anh_demo/`](./anh_demo/) chứa toàn bộ các ảnh chụp màn hình kiểm thử từ `00_mvnw_test.png` đến `06_nhac_han_tra.png`.
- **Bộ Kiểm thử API (Postman Collection):** File [`Digital_Library_Member2_Postman_Collection.json`](./Digital_Library_Member2_Postman_Collection.json) dùng để nhập vào Postman kiểm thử các REST API của hệ thống.

