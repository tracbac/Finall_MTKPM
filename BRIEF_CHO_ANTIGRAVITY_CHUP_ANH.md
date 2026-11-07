# Brief cho Antigravity: chạy dự án và chụp ảnh demo cho tiểu luận

## 1. Dự án là gì
Hệ thống Quản lý Thư viện số (Spring Boot 3.3.2, Java 21), môn "Mẫu thiết kế cho phần mềm". Thư mục: `Finall_MTKPM-main_da_ap_patch/Finall_MTKPM-main`. Giao diện web tĩnh nằm trong `src/main/resources/static`: `index.html`, `books.html`, `auth.html`, `my-books.html`, `admin.html`.

## 2. Cách chạy (BẮT BUỘC dùng profile `local`)
Profile mặc định nối Supabase thật. Hãy dùng H2 trong bộ nhớ để không làm bẩn dữ liệu thật:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```
Đợi dòng `Started DigitalLibraryApplication` và dòng `[DataSeeder2] Đã tạo 2 tài khoản`. App chạy tại http://localhost:8080.

Tài khoản mẫu:
- `admin / admin123` (quyền ADMIN)
- `reader / 123456` (quyền USER)

Kiểm thử: `.\mvnw.cmd test` cho kết quả `Tests run: 103, Failures: 0` (đã chạy thành công).

## 3. Những gì đã thay đổi so với bản gốc (để hiểu khi chụp)
1. **Observer (mới)**: dùng `ApplicationEventPublisher` và `@EventListener`. Các lớp mới nằm trong `service/service4/observer/`: `LibraryEvent4`, `LibraryEventType4`, `InAppNotificationListener4` (gửi thông báo trong ứng dụng), `NotificationAuditListener4` (ghi log kiểm toán ra console), `DueDateReminderScheduler4` (nhắc hạn trả). Các service mượn/trả/thêm sách giờ phát sự kiện thay vì gọi thẳng `NotificationService`.
2. **Quá hạn tự động**: `OverdueLoanScheduler4` tự rà soát phiếu quá hạn. Admin cũng có nút "Rà soát quá hạn".
3. **Khóa quyền khi quá hạn/nợ phạt**: `EBookDownloadProxy3` kiểm tra quá hạn ở cả hai luồng mượn. Nếu còn nợ thì trả HTTP 403 và vẫn ghi log DENIED (đã sửa lỗi rollback bằng `noRollbackFor`).
4. **Bảo mật tiền phạt**: chỉ ADMIN được mượn tại quầy, trả sách, xác nhận nộp phạt. Độc giả không còn nút tự nộp phạt; `my-books.html` hiện "Vui lòng nộp phạt tại quầy thủ thư". `admin.html` có nút "Xác nhận đã nộp phạt".
5. Endpoint mới: `POST /api/notifications/admin/due-reminders/run` (ADMIN) để chạy nhắc hạn ngay.

## 4. Danh sách ảnh cần chụp (PNG, toàn trang, độ phân giải rõ)
Đặt tên đúng như dưới đây và lưu vào thư mục `anh_demo/`:

| Tên file | Nội dung cần thấy |
|---|---|
| `00_mvnw_test.png` | Terminal cuối lệnh `mvnw test`: `Tests run: 103, Failures: 0, Errors: 0` và `BUILD SUCCESS` (user đã có ảnh này, lưu thành tên này) |
| `01_tim_kiem_loc_sach.png` | `books.html`: nhập từ khóa, chọn thể loại, bật "chỉ sách còn hàng", hiện kết quả lọc |
| `02_google_books_va_thong_bao.png` | Admin: tab Google Books, tìm "clean code", nhập một sách (loại E-Book). Sau đó đăng nhập reader, tab Thông báo hiện "📚 Sách mới: ..." (nên chụp 2 ảnh: `02a`, `02b`) |
| `03_muon_sach_giay.png` | Reader gửi yêu cầu mượn, Admin duyệt, rồi xác nhận trả. Chụp trang duyệt mượn của admin và tab Thông báo của reader (`03a`, `03b`). Console cũng in log của Observer thứ hai |
| `04_drm_tai_ebook.png` | Reader gửi yêu cầu mượn E-Book, Admin duyệt với 3 lượt tải. Chụp "Tủ sách E-Book & DRM", tải đến khi hết lượt và thấy thông báo lỗi hết lượt (HTTP 409) |
| `05_phat_qua_han_403.png` | Admin cho mượn tại quầy, đẩy hạn trả về quá khứ, bấm "Rà soát quá hạn", xác nhận trả để sinh biên lai phạt. Chụp bảng phiếu mượn của admin; reader tải E-Book bị 403 và hiện "Vui lòng nộp phạt tại quầy thủ thư"; admin bấm "Xác nhận đã nộp phạt" rồi reader tải lại được |
| `06_nhac_han_tra.png` | Reader có phiếu mượn đến hạn vào ngày mai. Admin gọi `POST /api/notifications/admin/due-reminders/run`. Reader thấy "⏰ Nhắc hạn: Sách ... đến hạn trả vào ngày ..." |

Gợi ý kỹ thuật:
- Để đẩy hạn trả về quá khứ trên H2 trong bộ nhớ, dùng H2 Console nếu đã bật trong `application-local.properties`, hoặc sửa tạm bằng API/SQL. Nếu cần, thêm endpoint test tạm rồi gỡ ra sau; KHÔNG commit phần thêm đó.
- Tránh lộ token, mật khẩu thật hay khóa API trong ảnh.
- Các nút xóa/xác nhận có thể bật hộp thoại `confirm`; nhớ bấm OK.

## 5. Sau khi chụp
Gửi lại toàn bộ thư mục `anh_demo/` cho người dùng. Bài tiểu luận (file .docx) có sẵn các ô "Chèn ảnh chụp..." ở mục 4.4 (kết quả test) và 4.5.1 đến 4.5.6 (6 kịch bản) để chèn ảnh vào.
