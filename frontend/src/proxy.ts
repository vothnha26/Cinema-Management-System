import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';
import { hasPermission } from './utils/rbac';

function decodeJwt(token: string) {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    const payload = parts[1];
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(decoded);
  } catch (e) {
    return null;
  }
}

export function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // Chỉ bảo vệ các route admin
  if (pathname.startsWith('/admin')) {
    const token = request.cookies.get('auth_token')?.value;

    if (!token) {
      // Chưa đăng nhập -> chuyển hướng về /auth
      return NextResponse.redirect(new URL('/auth?message=Vui+lòng+đăng+nhập+để+truy+cập!', request.url));
    }

    const decoded = decodeJwt(token);
    if (!decoded) {
      // Token lỗi -> xóa cookie và bắt đăng nhập lại
      const response = NextResponse.redirect(new URL('/auth?message=Phiên+làm+việc+không+hợp+lệ!', request.url));
      response.cookies.delete('auth_token');
      return response;
    }

    // Role được giải mã từ JWT (Spring Boot thường để key là 'role' hoặc 'roles' hoặc 'authorities')
    const role = decoded.role || decoded.roles || decoded.role_code;
    
    if (!hasPermission(role, 'ACCESS_ADMIN_DASHBOARD')) {
      // Không đủ thẩm quyền -> Chuyển về trang chủ
      return NextResponse.redirect(new URL('/?message=Bạn+không+có+quyền+truy+cập+phân+hệ+quản+trị!', request.url));
    }
  }

  return NextResponse.next();
}

// Cấu hình matcher để proxy chỉ chạy trên các route admin
export const config = {
  matcher: ['/admin/:path*'],
};
