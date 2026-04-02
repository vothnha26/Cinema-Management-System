package com.example.cinema.service.notification;

/**
 * Strategy Pattern (OCP + DIP):
 * Interface chung cho các kênh gửi thông báo.
 * Khi cần thêm kênh mới (SMS, Zalo, Push), chỉ cần tạo class mới implement
 * interface này mà KHÔNG sửa bất kỳ code nào đang có.
 */
public interface INotificationStrategy {

    /**
     * Gửi thông báo tới người nhận.
     *
     * @param to      địa chỉ nhận (email, số điện thoại, ...)
     * @param subject tiêu đề thông báo
     * @param body    nội dung thông báo
     */
    void send(String to, String subject, String body);

    /**
     * Trả về loại kênh gửi để Factory/Registry nhận diện.
     */
    String getChannel();
}
