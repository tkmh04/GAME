# 🚢 ĐỀ TÀI 16: GAME BATTLESHIP

## 1. Mô tả chức năng ứng dụng (Yêu cầu GUI)

### 🔹 Chức năng bắt buộc:
* ✔ **Vào game:** Nhập tên (không trùng, chỉ tồn tại khi online). 
* ✔ **Ghép ngẫu nhiên:** Tự động ghép 2 người vào phòng và chơi khi cả hai sẵn sàng. `0đ`
* ✔ **Đặt tàu:** Tự đặt 5 tàu (ngang/dọc) hoặc ngẫu nhiên, không chồng lấn. `1đ`
* ✔ **Màn hình chơi:** * Hiển thị bảng log (vị trí, trạng thái trúng/trượt).
    * Lưới 10x10 người chơi & lưới 10x10 đối thủ.
    * Thông báo thắng/thua. `0đ`
* ✔ **Chat:** Nhắn tin đơn giản giữa người chơi trong ván chơi. `0.5đ`
* ✔ **Xem trạng thái:** Xem phòng đang đấu bất kỳ, xem số người online.

### 🔸 Chức năng không bắt buộc:
* ❌ **Phòng riêng:** Tạo ID phòng (tồn tại 1 tiếng), xác nhận cùng vào chơi.
* ❌ **Mời chơi:** Mời người đang online vào ghép trận.
* ❌ **Thỏa thuận:** Chọn quy định (thời gian lượt, bên đi trước) trước khi bắt đầu.
* ❌ **Cửa hàng:** Bắn chìm toàn bộ tàu nhận tiền thưởng để mua Radar, vũ khí...
* ❌ **Chế độ khác:** Chơi với máy đơn giản hoặc chọn kích thước bảng, số lượng tàu.

---

## 2. Yêu cầu phía Server (Không cần GUI)
* ✔ **Quản lý Client:** Theo dõi trạng thái (online, đang chơi), gán mã tạm cho mỗi người.
* ✔ **Thống kê:** Xem số phòng mở, số ván đang chơi, tổng số ván/người chơi.
* ✔ **Xem Log:** Xem trận đấu theo mã (thời gian, tên user, các lượt đi, kết quả).

---

## 3. Yêu cầu chung
* ✔ Chạy trên các máy tính khác nhau (Socket).
* 50/50 **Mã hóa dữ liệu** giữa Client và Server.

# Cấu hình tiếng việt
 * Win + R
 * Gõ intl.cpl
 * Administrative, bấm vào Change system locale
 * Tích vào ô chọn: “Beta: Use Unicode UTF-8 for worldwide language support”
 
# Cấu hình JDK21
