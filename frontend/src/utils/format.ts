/**
 * Helper utility functions for formatting data in UI components
 */

/**
 * Định dạng số tiền sang định dạng VND (ví dụ: 75.000đ)
 */
export const formatPrice = (price: number): string => {
  if (price === undefined || price === null) return '0đ';
  return price.toLocaleString('vi-VN') + 'đ';
};

/**
 * Định dạng chuỗi ngày ISO hoặc Date thành định dạng giờ phút (ví dụ: 14:30)
 */
export const formatTime = (dateStr: string | Date): string => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return '';
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${hours}:${minutes}`;
};

/**
 * Định dạng chuỗi ngày ISO hoặc Date thành định dạng ngày tháng năm (ví dụ: 12/07/2026)
 */
export const formatDate = (dateStr: string | Date): string => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return '';
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const year = date.getFullYear();
  return `${day}/${month}/${year}`;
};

/**
 * Định dạng ngày và giờ đầy đủ (ví dụ: 14:30 12/07/2026)
 */
export const formatDateTime = (dateStr: string | Date): string => {
  if (!dateStr) return '';
  return `${formatTime(dateStr)} ${formatDate(dateStr)}`;
};
