'use client';

import { useState, useEffect, Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { Eye, EyeOff, Film, Check, Mail } from 'lucide-react';
import { authService } from '../../services/authService';
import { useAuthStore } from '../../store/authStore';
import { getRedirectPath } from '../../utils/rbac';

export default function AuthPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen flex items-center justify-center bg-[#FAFAFA]">
        <div className="text-[#6B7280] font-semibold">Đang tải trang đăng nhập...</div>
      </div>
    }>
      <AuthForm />
    </Suspense>
  );
}

function AuthForm() {
  const [mode, setMode] = useState<'login' | 'register' | 'forgot'>('login');
  const [forgotStep, setForgotStep] = useState(1);
  const [showPass, setShowPass] = useState(false);
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  
  // Trạng thái form
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [fullName, setFullName] = useState('');
  
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [loading, setLoading] = useState(false);

  const router = useRouter();
  const searchParams = useSearchParams();
  const { login: storeLogin, isAuthenticated, user } = useAuthStore();

  // Kiểm tra nếu đã đăng nhập thì tự động chuyển hướng
  useEffect(() => {
    if (isAuthenticated && user) {
      router.push(getRedirectPath(user.role));
    }

    const message = searchParams.get('message');
    if (message) {
      setErrorMsg(message);
    }
  }, [isAuthenticated, user, router, searchParams]);

  const handleOtpChange = (i: number, val: string) => {
    if (val.length > 1) return;
    const next = [...otp];
    next[i] = val;
    setOtp(next);
    if (val && i < 5) {
      const nextInput = document.getElementById(`otp-${i + 1}`);
      nextInput?.focus();
    }
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg('');
    setSuccessMsg('');
    if (!email || !password) {
      setErrorMsg('Vui lòng điền đầy đủ email và mật khẩu!');
      return;
    }

    setLoading(true);
    try {
      // Spring Boot AuthController nhận LoginRequest (username, password)
      // trong đó username chính là email người dùng
      const responseData = await authService.login({
        username: email,
        password: password,
      });

      if (responseData && responseData.success) {
        const { token, username, role, fullName, branchId } = responseData.data;
        
        const userData = {
          username: username,
          email: username,
          fullName: fullName || username.split('@')[0],
          role: role,
          branchId: branchId
        };

        storeLogin(token, userData as any);
        setSuccessMsg('Đăng nhập thành công!');
        
        // Chuyển hướng theo vai trò (Role)
        setTimeout(() => {
          router.push(getRedirectPath(role));
        }, 800);
      } else {
        setErrorMsg(responseData?.message || 'Đăng nhập thất bại!');
      }
    } catch (err: any) {
      console.error(err);
      setErrorMsg(err.response?.data?.message || 'Email hoặc mật khẩu không chính xác!');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg('');
    setSuccessMsg('');

    if (!fullName || !email || !password || !confirmPassword) {
      setErrorMsg('Vui lòng điền đầy đủ thông tin đăng ký!');
      return;
    }

    if (password !== confirmPassword) {
      setErrorMsg('Mật khẩu xác nhận không khớp!');
      return;
    }

    setLoading(true);
    try {
      // API Register nhận RegisterRequest: fullName, email, password
      const responseData = await authService.register({
        fullName,
        email,
        password,
      });

      if (responseData && responseData.success) {
        setSuccessMsg('Đăng ký tài khoản thành công! Hãy đăng nhập.');
        setMode('login');
        setPassword('');
        setConfirmPassword('');
      } else {
        setErrorMsg(responseData?.message || 'Đăng ký thất bại!');
      }
    } catch (err: any) {
      console.error(err);
      setErrorMsg(err.response?.data?.message || 'Đăng ký thất bại, email có thể đã được sử dụng!');
    } finally {
      setLoading(false);
    }
  };

  return (
    // Trick: fixed inset-0 z-50 bg-[#FAFAFA] để che đi Navbar/Footer của layout cha
    <div className="fixed inset-0 z-50 bg-[#FAFAFA] flex overflow-y-auto">
      {/* Left panel (Illustration) */}
      <div className="hidden lg:flex w-1/2 bg-[#FEF3DC] items-center justify-center p-12 relative overflow-hidden">
        <div className="absolute inset-0 opacity-10">
          {Array.from({ length: 8 }).map((_, i) => (
            <div
              key={i}
              className="absolute rounded-full bg-[#F5A623]"
              style={{
                width: `${60 + i * 20}px`,
                height: `${60 + i * 20}px`,
                left: `${(i % 3) * 35}%`,
                top: `${Math.floor(i / 3) * 35}%`,
              }}
            />
          ))}
        </div>
        <div className="relative text-center">
          <div className="w-72 h-56 mx-auto mb-8 relative">
            <div className="absolute inset-0 bg-[#1C1C22] rounded-2xl" />
            <div className="absolute top-4 left-1/2 -translate-x-1/2 w-40 h-2 bg-white/20 rounded-full" />
            <div className="absolute bottom-8 left-1/2 -translate-x-1/2 w-48 h-24 bg-[#F5A623]/20 rounded-xl flex items-end justify-center pb-3">
              <div className="grid grid-cols-8 gap-1">
                {Array.from({ length: 32 }).map((_, i) => (
                  <div
                    key={i}
                    className="w-3 h-3 rounded-sm"
                    style={{ background: [8, 13, 19, 20].includes(i) ? '#F5A623' : 'rgba(255,255,255,0.2)' }}
                  />
                ))}
              </div>
            </div>
          </div>
          <h2 className="text-2xl font-extrabold text-[#22232B] mb-3">Trải nghiệm điện ảnh đỉnh cao</h2>
          <p className="text-[#6B7280] text-sm leading-relaxed max-w-xs mx-auto">
            Đặt vé nhanh chóng, chọn ghế yêu thích, nhận ưu đãi thành viên hấp dẫn.
          </p>
          <div className="flex justify-center gap-6 mt-8">
            {['100K+', '15', '4.9★'].map((stat, i) => (
              <div key={i} className="text-center">
                <div className="text-xl font-extrabold text-[#22232B]">{stat}</div>
                <div className="text-xs text-[#6B7280]">
                  {['Người dùng', 'Chi nhánh', 'Đánh giá'][i]}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Right panel (Form) */}
      <div className="flex-1 flex items-center justify-center p-6 bg-white lg:bg-transparent">
        <div className="w-full max-w-sm">
          {/* Logo */}
          <Link href="/" className="flex items-center gap-2 mb-8 inline-block">
            <div className="flex items-center gap-2">
              <div className="w-9 h-9 rounded-xl bg-[#F5A623] flex items-center justify-center">
                <Film className="w-5 h-5 text-white" />
              </div>
              <span className="font-bold text-xl text-[#22232B]">StarCinema</span>
            </div>
          </Link>

          {errorMsg && (
            <div className="bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-xl mb-4">
              {errorMsg}
            </div>
          )}

          {successMsg && (
            <div className="bg-green-50 border border-green-200 text-green-700 text-sm px-4 py-3 rounded-xl mb-4">
              {successMsg}
            </div>
          )}

          {mode !== 'forgot' ? (
            <form onSubmit={mode === 'login' ? handleLogin : handleRegister}>
              {/* Tab selector */}
              <div className="flex bg-[#F4F4F5] rounded-xl p-1 mb-7">
                {(['login', 'register'] as const).map((m) => (
                  <button
                    key={m}
                    type="button"
                    onClick={() => {
                      setMode(m);
                      setErrorMsg('');
                      setSuccessMsg('');
                    }}
                    className={`flex-1 py-2.5 rounded-lg text-sm font-semibold transition-all ${
                      mode === m ? 'bg-white text-[#22232B] shadow-sm' : 'text-[#6B7280]'
                    }`}
                  >
                    {m === 'login' ? 'Đăng nhập' : 'Đăng ký'}
                  </button>
                ))}
              </div>

              <h1 className="text-2xl font-extrabold text-[#22232B] mb-1">
                {mode === 'login' ? 'Chào mừng trở lại!' : 'Tạo tài khoản mới'}
              </h1>
              <p className="text-sm text-[#6B7280] mb-7">
                {mode === 'login' ? 'Đăng nhập để tiếp tục đặt vé.' : 'Đăng ký để nhận ưu đãi thành viên.'}
              </p>

              <div className="space-y-4">
                {mode === 'register' && (
                  <div>
                    <label className="block text-sm font-medium text-[#22232B] mb-1.5">Họ và tên</label>
                    <input
                      type="text"
                      required
                      value={fullName}
                      onChange={(e) => setFullName(e.target.value)}
                      placeholder="Nguyễn Văn A"
                      className="w-full px-4 py-3 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/20 transition-all"
                    />
                  </div>
                )}
                <div>
                  <label className="block text-sm font-medium text-[#22232B] mb-1.5">Email</label>
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="email@example.com"
                    className="w-full px-4 py-3 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/20 transition-all"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-[#22232B] mb-1.5">Mật khẩu</label>
                  <div className="relative">
                    <input
                      type={showPass ? 'text' : 'password'}
                      required
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      placeholder="••••••••"
                      className="w-full px-4 py-3 pr-12 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/20 transition-all"
                    />
                    <button
                      type="button"
                      onClick={() => setShowPass(!showPass)}
                      className="absolute right-4 top-1/2 -translate-y-1/2 text-[#6B7280] hover:text-[#22232B]"
                    >
                      {showPass ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                    </button>
                  </div>
                </div>
                {mode === 'register' && (
                  <div>
                    <label className="block text-sm font-medium text-[#22232B] mb-1.5">Xác nhận mật khẩu</label>
                    <input
                      type="password"
                      required
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      placeholder="••••••••"
                      className="w-full px-4 py-3 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/20 transition-all"
                    />
                  </div>
                )}
              </div>

              {mode === 'login' && (
                <div className="flex justify-end mt-2">
                  <button
                    type="button"
                    onClick={() => setMode('forgot')}
                    className="text-xs text-[#F5A623] hover:text-[#C47D0A] font-medium"
                  >
                    Quên mật khẩu?
                  </button>
                </div>
              )}

              <button
                type="submit"
                disabled={loading}
                className="w-full mt-6 py-3.5 rounded-xl bg-[#F5A623] text-white font-bold hover:bg-[#E09415] transition-all shadow-md shadow-[#F5A623]/25 active:scale-[0.98] disabled:opacity-50"
              >
                {loading ? 'Đang xử lý...' : mode === 'login' ? 'Đăng nhập' : 'Tạo tài khoản'}
              </button>

              <div className="flex items-center gap-3 my-5">
                <div className="flex-1 h-px bg-black/10" />
                <span className="text-xs text-[#6B7280]">Hoặc</span>
                <div className="flex-1 h-px bg-black/10" />
              </div>

              <button
                type="button"
                onClick={() => alert('Chức năng đăng nhập Google đang được phát triển!')}
                className="w-full py-3 rounded-xl border border-black/10 bg-white text-sm font-semibold text-[#22232B] hover:bg-black/5 transition-all flex items-center justify-center gap-3"
              >
                <svg viewBox="0 0 24 24" className="w-4 h-4">
                  <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                  <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                  <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                  <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
                </svg>
                Đăng nhập bằng Google
              </button>
            </form>
          ) : (
            /* Forgot password flow */
            <div>
              {/* Step indicator */}
              <div className="flex items-center gap-2 mb-8">
                {[1, 2, 3].map((s) => (
                  <div key={s} className="flex items-center gap-2">
                    <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold transition-all ${
                      forgotStep > s ? 'bg-[#F5A623] text-white' :
                      forgotStep === s ? 'bg-[#22232B] text-white' :
                      'bg-[#F4F4F5] text-[#6B7280]'
                    }`}>
                      {forgotStep > s ? <Check className="w-4 h-4" /> : s}
                    </div>
                    {s < 3 && <div className={`h-px w-8 transition-all ${forgotStep > s ? 'bg-[#F5A623]' : 'bg-black/10'}`} />}
                  </div>
                ))}
              </div>

              {forgotStep === 1 && (
                <>
                  <h1 className="text-2xl font-extrabold text-[#22232B] mb-1">Quên mật khẩu?</h1>
                  <p className="text-sm text-[#6B7280] mb-7">Nhập email để nhận mã xác nhận.</p>
                  <div className="relative mb-5">
                    <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-[#6B7280]" />
                    <input
                      type="email"
                      required
                      placeholder="email@example.com"
                      className="w-full pl-11 pr-4 py-3 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/20 transition-all"
                    />
                  </div>
                  <button onClick={() => setForgotStep(2)} className="w-full py-3.5 rounded-xl bg-[#F5A623] text-white font-bold hover:bg-[#E09415] transition-all">
                    Gửi mã OTP
                  </button>
                </>
              )}

              {forgotStep === 2 && (
                <>
                  <h1 className="text-2xl font-extrabold text-[#22232B] mb-1">Nhập mã OTP</h1>
                  <p className="text-sm text-[#6B7280] mb-7">Mã đã được gửi đến email của bạn.</p>
                  <div className="flex gap-2 justify-center mb-5">
                    {otp.map((v, i) => (
                      <input
                        key={i}
                        id={`otp-${i}`}
                        type="text"
                        inputMode="numeric"
                        maxLength={1}
                        value={v}
                        onChange={(e) => handleOtpChange(i, e.target.value)}
                        className="w-11 h-14 text-center text-xl font-bold rounded-xl border border-black/10 bg-white outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/20 transition-all"
                      />
                    ))}
                  </div>
                  <button onClick={() => setForgotStep(3)} className="w-full py-3.5 rounded-xl bg-[#F5A623] text-white font-bold hover:bg-[#E09415] transition-all">
                    Xác nhận
                  </button>
                </>
              )}

              {forgotStep === 3 && (
                <>
                  <h1 className="text-2xl font-extrabold text-[#22232B] mb-1">Đặt mật khẩu mới</h1>
                  <p className="text-sm text-[#6B7280] mb-7">Mật khẩu mới phải có ít nhất 8 ký tự.</p>
                  <div className="space-y-4 mb-5">
                    <input
                      type="password"
                      placeholder="Mật khẩu mới"
                      className="w-full px-4 py-3 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/20 transition-all"
                    />
                    <input
                      type="password"
                      placeholder="Xác nhận mật khẩu"
                      className="w-full px-4 py-3 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] focus:ring-2 focus:ring-[#F5A623]/20 transition-all"
                    />
                  </div>
                  <button onClick={() => { setMode('login'); setForgotStep(1) }} className="w-full py-3.5 rounded-xl bg-[#F5A623] text-white font-bold hover:bg-[#E09415] transition-all">
                    Đặt mật khẩu
                  </button>
                </>
              )}

              <button
                type="button"
                onClick={() => { setMode('login'); setForgotStep(1); }}
                className="w-full mt-4 text-sm text-[#6B7280] hover:text-[#22232B] text-center block"
              >
                ← Quay lại đăng nhập
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
