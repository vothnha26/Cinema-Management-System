'use client';

import { useState, useEffect } from 'react';
import { DollarSign, Ticket, Users, RefreshCw, BarChart2, TrendingUp, Calendar, Clock, Award, Star } from 'lucide-react';
import { AreaChart, Area, BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import { adminDashboardService } from '../../services/admin';

export default function AdminDashboardPage() {
  const [stats, setStats] = useState<any | null>(null);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');

  // Bộ lọc ngày
  const [startDate, setStartDate] = useState<string>(() => {
    const d = new Date();
    d.setDate(d.getDate() - 7); // 7 ngày trước
    return d.toISOString().split('T')[0];
  });
  const [endDate, setEndDate] = useState<string>(() => {
    return new Date().toISOString().split('T')[0];
  });

  const [startTime, setStartTime] = useState('00:00');
  const [endTime, setEndTime] = useState('23:59');
  const [lastUpdated, setLastUpdated] = useState('');

  const fetchStats = async () => {
    setLoading(true);
    setErrorMsg('');
    try {
      const res = await adminDashboardService.getOverviewStats({
        startDate,
        endDate,
        startTime: `${startTime}:00`,
        endTime: `${endTime}:59`,
      });

      if (res?.success) {
        setStats(res.data);
        setLastUpdated(new Date().toLocaleTimeString('vi-VN'));
      } else {
        setErrorMsg(res?.message || 'Không thể tải số liệu thống kê.');
      }
    } catch (err: any) {
      console.error(err);
      setErrorMsg(err.response?.data?.message || 'Có lỗi xảy ra khi kết nối máy chủ.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, []);

  // Format dữ liệu cho Recharts
  const getDayChartData = () => {
    if (!stats?.revenueByDay) return [];
    return Object.entries(stats.revenueByDay).map(([day, revenue]) => ({
      name: day,
      'Doanh thu': revenue,
    }));
  };

  const getHourChartData = () => {
    if (!stats?.revenueByHour) return [];
    // Tạo 24 giờ đầy đủ
    return Array.from({ length: 24 }, (_, h) => {
      return {
        name: `${h}h`,
        'Doanh thu': stats.revenueByHour[h] || 0,
      };
    });
  };

  const getSortedMovieRevenue = () => {
    if (!stats?.revenueByMovie) return [];
    return Object.entries(stats.revenueByMovie)
      .map(([title, revenue]) => ({ title, revenue: Number(revenue) }))
      .sort((a, b) => b.revenue - a.revenue);
  };

  const movieRevenueList = getSortedMovieRevenue();
  const maxMovieRevenue = movieRevenueList.length > 0 ? movieRevenueList[0].revenue : 1;

  return (
    <div className="space-y-6">
      {/* Header section */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-[#22232B] flex items-center gap-2">
            <BarChart2 className="w-6 h-6 text-[#F5A623]" />
            Bảng điều khiển & Báo cáo Chiến lược
          </h1>
          <p className="text-sm text-[#6B7280]">
            Tổng hợp dữ liệu kinh doanh rạp phim, phân bổ doanh thu theo giờ và ngày
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2 text-xs text-[#6B7280] font-semibold">
          {lastUpdated && (
            <span className="bg-black/5 px-3 py-2 rounded-xl border border-black/5">
              Cập nhật lúc: {lastUpdated}
            </span>
          )}
          <button
            onClick={fetchStats}
            className="p-2.5 bg-white border border-black/10 text-[#6B7280] rounded-xl hover:bg-black/5 transition-colors"
            title="Làm mới"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="bg-white border border-black/5 p-4 rounded-2xl shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4 flex-wrap">
        <div className="flex flex-wrap items-center gap-3">
          <div className="flex items-center gap-2">
            <span className="text-xs font-bold text-[#6B7280] uppercase tracking-wider">Từ ngày:</span>
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="px-3 py-2 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B] font-semibold"
            />
          </div>

          <div className="flex items-center gap-2">
            <span className="text-xs font-bold text-[#6B7280] uppercase tracking-wider">Đến ngày:</span>
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="px-3 py-2 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B] font-semibold"
            />
          </div>

          <div className="w-[1px] h-6 bg-black/10 hidden md:block" />

          <div className="flex items-center gap-1.5">
            <span className="text-xs font-bold text-[#6B7280] uppercase tracking-wider">Khung giờ:</span>
            <input
              type="time"
              value={startTime}
              onChange={(e) => setStartTime(e.target.value)}
              className="px-2 py-1.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B] font-semibold"
            />
            <span className="text-[#6B7280] font-semibold">-</span>
            <input
              type="time"
              value={endTime}
              onChange={(e) => setEndTime(e.target.value)}
              className="px-2 py-1.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B] font-semibold"
            />
          </div>
        </div>

        <button
          onClick={fetchStats}
          className="px-5 py-2.5 bg-[#F5A623] text-white rounded-xl font-bold hover:bg-[#E09415] transition-all text-xs active:scale-[0.98] shadow-md shadow-[#F5A623]/25"
        >
          Lọc dữ liệu
        </button>
      </div>

      {errorMsg && (
        <div className="bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-xl">
          {errorMsg}
        </div>
      )}

      {/* Overview Stat Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Doanh thu */}
        <div className="bg-white border border-black/5 rounded-2xl p-5 flex items-center gap-4 hover:shadow-md transition-all shadow-sm">
          <div className="w-12 h-12 rounded-2xl bg-[#EF4444]/10 text-[#EF4444] flex items-center justify-center">
            <DollarSign className="w-6 h-6" />
          </div>
          <div>
            <div className="text-xs font-bold text-[#6B7280] uppercase tracking-wider">Tổng Doanh Thu</div>
            <div className="text-2xl font-extrabold text-[#22232B] mt-0.5">
              {loading ? '...' : `${new Intl.NumberFormat('vi-VN').format(stats?.totalRevenue || 0)}đ`}
            </div>
          </div>
        </div>

        {/* Vé đã bán */}
        <div className="bg-white border border-black/5 rounded-2xl p-5 flex items-center gap-4 hover:shadow-md transition-all shadow-sm">
          <div className="w-12 h-12 rounded-2xl bg-green-500/10 text-green-600 flex items-center justify-center">
            <Ticket className="w-6 h-6" />
          </div>
          <div>
            <div className="text-xs font-bold text-[#6B7280] uppercase tracking-wider">Vé Đã Bán</div>
            <div className="text-2xl font-extrabold text-[#22232B] mt-0.5">
              {loading ? '...' : new Intl.NumberFormat('vi-VN').format(stats?.totalTicketsSold || 0)}
            </div>
          </div>
        </div>

        {/* Khách hàng mới */}
        <div className="bg-white border border-black/5 rounded-2xl p-5 flex items-center gap-4 hover:shadow-md transition-all shadow-sm">
          <div className="w-12 h-12 rounded-2xl bg-[#F5A623]/10 text-[#F5A623] flex items-center justify-center">
            <Users className="w-6 h-6" />
          </div>
          <div>
            <div className="text-xs font-bold text-[#6B7280] uppercase tracking-wider">Khách Hàng Mới</div>
            <div className="text-2xl font-extrabold text-[#22232B] mt-0.5">
              {loading ? '...' : new Intl.NumberFormat('vi-VN').format(stats?.newCustomersCount || 0)}
            </div>
          </div>
        </div>
      </div>

      {/* Detail Chart Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Doanh thu theo phim */}
        <div className="bg-white border border-black/5 rounded-2xl p-6 shadow-sm flex flex-col justify-between">
          <div>
            <h3 className="font-bold text-base text-[#22232B] flex items-center gap-1.5 mb-6">
              <Award className="w-5 h-5 text-[#F5A623]" /> Top Doanh Thu Theo Phim
            </h3>
            {loading ? (
              <div className="text-center py-20 text-sm text-[#6B7280]">Đang tải dữ liệu phim...</div>
            ) : movieRevenueList.length === 0 ? (
              <div className="text-center py-20 text-sm text-[#6B7280]">Không có dữ liệu phim</div>
            ) : (
              <div className="space-y-4">
                {movieRevenueList.map((m, idx) => {
                  const percent = (m.revenue / maxMovieRevenue) * 100;
                  return (
                    <div key={idx} className="space-y-1.5">
                      <div className="flex items-center justify-between text-xs font-bold">
                        <span className="text-[#22232B] line-clamp-1 flex items-center gap-1.5">
                          <Star className="w-3.5 h-3.5 fill-[#F5A623] text-[#F5A623]" />
                          {m.title}
                        </span>
                        <span className="text-[#EF4444] shrink-0">
                          {new Intl.NumberFormat('vi-VN').format(m.revenue)}đ
                        </span>
                      </div>
                      <div className="h-2 w-full bg-black/5 rounded-full overflow-hidden">
                        <div
                          className="h-full bg-gradient-to-r from-[#EF4444] to-[#F5A623] rounded-full transition-all duration-1000"
                          style={{ width: `${percent}%` }}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>

        {/* Phân bổ theo giờ */}
        <div className="bg-white border border-black/5 rounded-2xl p-6 shadow-sm flex flex-col justify-between">
          <div>
            <h3 className="font-bold text-base text-[#22232B] flex items-center gap-1.5 mb-6">
              <Clock className="w-5 h-5 text-indigo-600" /> Phân Bổ Doanh Thu Theo Giờ
            </h3>
            {loading ? (
              <div className="text-center py-20 text-sm text-[#6B7280]">Đang tải dữ liệu biểu đồ...</div>
            ) : (
              <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={getHourChartData()}>
                    <XAxis dataKey="name" stroke="#6B7280" fontSize={10} tickLine={false} axisLine={false} />
                    <YAxis
                      stroke="#6B7280"
                      fontSize={10}
                      tickLine={false}
                      axisLine={false}
                      tickFormatter={(value) => `${value / 1000000}M`}
                    />
                    <Tooltip
                      formatter={(value: any) => [`${new Intl.NumberFormat('vi-VN').format(value)}đ`, 'Doanh thu']}
                      contentStyle={{ borderRadius: '12px', border: '1px solid rgba(0,0,0,0.05)', fontSize: '12px' }}
                    />
                    <Bar dataKey="Doanh thu" fill="#EF4444" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            )}
          </div>
        </div>

        {/* Xu hướng doanh thu theo ngày */}
        <div className="bg-white border border-black/5 rounded-2xl p-6 shadow-sm lg:col-span-2">
          <h3 className="font-bold text-base text-[#22232B] flex items-center gap-1.5 mb-6">
            <TrendingUp className="w-5 h-5 text-green-600" /> Xu Hướng Doanh Thu Theo Ngày
          </h3>
          {loading ? (
            <div className="text-center py-20 text-sm text-[#6B7280]">Đang tải dữ liệu xu hướng...</div>
          ) : (
            <div className="h-72">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={getDayChartData()}>
                  <defs>
                    <linearGradient id="colorRevenue" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#EF4444" stopOpacity={0.2} />
                      <stop offset="95%" stopColor="#EF4444" stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <XAxis dataKey="name" stroke="#6B7280" fontSize={10} tickLine={false} axisLine={false} />
                  <YAxis
                    stroke="#6B7280"
                    fontSize={10}
                    tickLine={false}
                    axisLine={false}
                    tickFormatter={(value) => `${value / 1000000}M`}
                  />
                  <Tooltip
                    formatter={(value: any) => [`${new Intl.NumberFormat('vi-VN').format(value)}đ`, 'Doanh thu']}
                    contentStyle={{ borderRadius: '12px', border: '1px solid rgba(0,0,0,0.05)', fontSize: '12px' }}
                  />
                  <Area type="monotone" dataKey="Doanh thu" stroke="#EF4444" strokeWidth={2} fillOpacity={1} fill="url(#colorRevenue)" />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
