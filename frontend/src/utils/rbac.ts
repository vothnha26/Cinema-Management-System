export type Role = 'ADMIN' | 'MANAGER' | 'STAFF' | 'CUSTOMER';

export type Permission = 
  | 'ACCESS_ADMIN_DASHBOARD' 
  | 'ACCESS_POS' 
  | 'MANAGE_USERS' 
  | 'MANAGE_PRICING' 
  | 'MANAGE_MOVIES' 
  | 'BOOK_TICKETS';

/**
 * Bản đồ phân phối quyền hạn dựa trên vai trò (RBAC Matrix)
 * Giúp tuân thủ nguyên tắc Open/Closed (OCP) - Khi thêm vai trò hoặc quyền mới, chỉ cần khai báo thêm tại đây.
 */
export const ROLE_PERMISSIONS: Record<Role, Permission[]> = {
  ADMIN: [
    'ACCESS_ADMIN_DASHBOARD',
    'ACCESS_POS',
    'MANAGE_USERS',
    'MANAGE_PRICING',
    'MANAGE_MOVIES',
    'BOOK_TICKETS'
  ],
  MANAGER: [
    'ACCESS_ADMIN_DASHBOARD',
    'ACCESS_POS',
    'MANAGE_MOVIES',
    'BOOK_TICKETS'
  ],
  STAFF: [
    'ACCESS_POS',
    'BOOK_TICKETS'
  ],
  CUSTOMER: [
    'BOOK_TICKETS'
  ]
};

/**
 * Kiểm tra xem một vai trò có quyền thực hiện hành động cụ thể hay không
 */
export const hasPermission = (role: Role | string | undefined, permission: Permission): boolean => {
  if (!role) return false;
  const normalizedRole = role.toUpperCase() as Role;
  const permissions = ROLE_PERMISSIONS[normalizedRole];
  return permissions ? permissions.includes(permission) : false;
};

/**
 * Lấy đường dẫn điều hướng mặc định sau khi đăng nhập dựa trên vai trò của người dùng
 */
export const getRedirectPath = (role: Role | string | undefined): string => {
  if (!role) return '/auth';
  const normalizedRole = role.toUpperCase() as Role;

  if (hasPermission(normalizedRole, 'ACCESS_ADMIN_DASHBOARD')) {
    return '/admin';
  }
  if (hasPermission(normalizedRole, 'ACCESS_POS')) {
    return '/pos';
  }
  return '/';
};
