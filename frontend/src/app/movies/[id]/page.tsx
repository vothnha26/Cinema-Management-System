'use client';

import { useState, useEffect, use } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Star, Play, Clock, Calendar as CalendarIcon, ChevronLeft, X } from 'lucide-react';
import { movieService } from '../../../services/movieService';

interface Movie {
  id: number;
  title: string;
  genre: string;
  duration: number;
  rating: number;
  posterUrl: string;
  backdropUrl?: string;
  trailerUrl?: string;
  releaseDate?: string;
  synopsis?: string;
  director?: string;
}

interface Showtime {
  id: number;
  startTime: string; // ISO date time
  format: string; // 2D, 3D, IMAX...
  roomName: string;
  branchName: string;
  branchId: number;
  price: number;
}

// Group showtimes by branch name
interface BranchGroup {
  branchName: string;
  branchId: number;
  showtimes: Showtime[];
}

export default function MovieDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const resolvedParams = use(params);
  const movieId = resolvedParams.id;
  const router = useRouter();

  const [movie, setMovie] = useState<Movie | null>(null);
  const [dates, setDates] = useState<string[]>([]);
  const [selectedDate, setSelectedDate] = useState('');
  const [branchGroups, setBranchGroups] = useState<BranchGroup[]>([]);
  
  const [activeTab, setActiveTab] = useState<'info' | 'reviews'>('info');
  const [showTrailer, setShowTrailer] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchMovieData = async () => {
      setLoading(true);
      try {
        // 1. Tải thông tin phim
        const movieRes = await movieService.getMovieDetails(movieId);
        if (movieRes?.data) {
          setMovie(movieRes.data);
        }

        // 2. Tải danh sách ngày chiếu có lịch của phim này
        const datesRes = await movieService.getShowtimeDates(movieId);
        if (datesRes?.data && datesRes.data.length > 0) {
          setDates(datesRes.data);
          setSelectedDate(datesRes.data[0]); // Mặc định chọn ngày đầu tiên
        } else {
          // Tạo một vài ngày mặc định nếu backend không có lịch chiếu
          const today = new Date();
          const defaultDates = Array.from({ length: 5 }).map((_, i) => {
            const d = new Date(today);
            d.setDate(today.getDate() + i);
            return d.toISOString().split('T')[0];
          });
          setDates(defaultDates);
          setSelectedDate(defaultDates[0]);
        }
      } catch (err) {
        console.error('Lỗi khi tải thông tin phim:', err);
        // Fallback demo movie
        setMovie({
          id: Number(movieId),
          title: 'Dune: Phần Hai',
          genre: 'Khoa học viễn tưởng',
          duration: 166,
          rating: 8.6,
          posterUrl: 'https://images.unsplash.com/photo-1518676590629-3dcbd9c5a5c9?w=300&h=450&fit=crop&auto=format',
          backdropUrl: 'https://images.unsplash.com/photo-1518676590629-3dcbd9c5a5c9?w=1400&h=500&fit=crop&auto=format',
          trailerUrl: 'https://www.youtube.com/embed/U2Qp5pL3C-o',
          synopsis: 'Paul Atreides đoàn kết với người Fremen trong cuộc chiến thần thánh chống lại những kẻ âm mưu đã hủy hoại gia đình anh. Đối mặt với lựa chọn giữa tình yêu cuộc đời mình và số phận của vũ trụ, anh phải ngăn chặn một tương lai khủng khiếp chỉ có anh mới có thể nhìn thấy.',
          director: 'Denis Villeneuve',
          releaseDate: '2024-03-01',
        });
        const today = new Date();
        const defaultDates = Array.from({ length: 5 }).map((_, i) => {
          const d = new Date(today);
          d.setDate(today.getDate() + i);
          return d.toISOString().split('T')[0];
        });
        setDates(defaultDates);
        setSelectedDate(defaultDates[0]);
      } finally {
        setLoading(false);
      }
    };

    fetchMovieData();
  }, [movieId]);

  // Load suất chiếu khi chọn ngày khác nhau
  useEffect(() => {
    if (!selectedDate) return;

    const fetchShowtimes = async () => {
      try {
        const res = await movieService.getShowtimes(movieId, selectedDate);
        if (res?.data && res.data.length > 0) {
          const rawShowtimes: Showtime[] = res.data;
          
          // Phân nhóm suất chiếu theo chi nhánh
          const groupsMap = new Map<string, { branchId: number; showtimes: Showtime[] }>();
          rawShowtimes.forEach((st) => {
            const key = st.branchName || 'Chi nhánh StarCinema';
            if (!groupsMap.has(key)) {
              groupsMap.set(key, { branchId: st.branchId, showtimes: [] });
            }
            groupsMap.get(key)!.showtimes.push(st);
          });

          const groupsArray: BranchGroup[] = Array.from(groupsMap.entries()).map(([name, val]) => ({
            branchName: name,
            branchId: val.branchId,
            showtimes: val.showtimes.sort((a, b) => a.startTime.localeCompare(b.startTime)),
          }));

          setBranchGroups(groupsArray);
        } else {
          // Fallback demo suất chiếu cho ngày đã chọn
          setBranchGroups([
            {
              branchName: 'StarCinema Quận 1',
              branchId: 1,
              showtimes: [
                { id: 101, startTime: `${selectedDate}T09:30:00`, format: '2D', roomName: 'Phòng 1', branchName: 'StarCinema Quận 1', branchId: 1, price: 85000 },
                { id: 102, startTime: `${selectedDate}T13:00:00`, format: '3D', roomName: 'Phòng 2', branchName: 'StarCinema Quận 1', branchId: 1, price: 110000 },
                { id: 103, startTime: `${selectedDate}T18:30:00`, format: 'IMAX', roomName: 'Phòng 3', branchName: 'StarCinema Quận 1', branchId: 1, price: 150000 },
              ],
            },
            {
              branchName: 'StarCinema Quận 7',
              branchId: 2,
              showtimes: [
                { id: 201, startTime: `${selectedDate}T10:15:00`, format: '2D', roomName: 'Phòng 1', branchName: 'StarCinema Quận 7', branchId: 2, price: 85000 },
                { id: 202, startTime: `${selectedDate}T15:45:00`, format: '2D', roomName: 'Phòng 2', branchName: 'StarCinema Quận 7', branchId: 2, price: 85000 },
              ],
            },
          ]);
        }
      } catch (err) {
        console.error('Lỗi khi tải suất chiếu:', err);
        setBranchGroups([]);
      }
    };

    fetchShowtimes();
  }, [selectedDate, movieId]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#FAFAFA]">
        <div className="text-[#6B7280] font-semibold">Đang tải thông tin phim...</div>
      </div>
    );
  }

  if (!movie) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#FAFAFA]">
        <div className="text-center">
          <h1 className="text-2xl font-bold mb-4">Không tìm thấy phim</h1>
          <Link href="/" className="px-6 py-2 bg-[#F5A623] text-white rounded-xl">Quay lại trang chủ</Link>
        </div>
      </div>
    );
  }

  const formatColor = (fmt: string) => {
    if (fmt === 'IMAX') return 'bg-[#1C1C22] text-white';
    if (fmt === '3D') return 'bg-[#F5A623]/15 text-[#C47D0A]';
    return 'bg-[#F4F4F5] text-[#22232B]';
  };

  const formatShowTime = (isoString: string) => {
    try {
      const parts = isoString.split('T');
      if (parts.length > 1) {
        return parts[1].substring(0, 5); // Lấy "HH:MM"
      }
      return isoString;
    } catch {
      return isoString;
    }
  };

  const formatDateLabel = (dateStr: string) => {
    try {
      const d = new Date(dateStr);
      const weekday = d.toLocaleDateString('vi-VN', { weekday: 'short' });
      const day = d.getDate();
      const month = d.getMonth() + 1;
      return { weekday, label: `${day}/${month}` };
    } catch {
      return { weekday: '', label: dateStr };
    }
  };

  return (
    <div className="min-h-screen bg-[#FAFAFA]">
      {/* Hero Backdrop */}
      <div className="relative w-full h-80">
        <img src={movie.backdropUrl || movie.posterUrl} alt={movie.title} className="w-full h-full object-cover" />
        <div className="absolute inset-0 bg-gradient-to-r from-[#1C1C22]/70 via-[#1C1C22]/30 to-transparent" />
        <div className="absolute inset-0 bg-gradient-to-t from-[#FAFAFA] via-transparent to-transparent" />

        <button
          onClick={() => router.back()}
          className="absolute top-6 left-6 w-10 h-10 rounded-full bg-white/20 backdrop-blur-sm text-white hover:bg-white/30 transition-all flex items-center justify-center"
        >
          <ChevronLeft className="w-5 h-5" />
        </button>
      </div>

      {/* Movie Main Info */}
      <div className="max-w-5xl mx-auto px-6 -mt-24 relative z-10 pb-16">
        <div className="flex flex-col sm:flex-row gap-8">
          {/* Poster */}
          <div className="shrink-0 flex justify-center">
            <img
              src={movie.posterUrl}
              alt={movie.title}
              className="w-40 h-60 object-cover rounded-2xl shadow-xl border border-black/5"
            />
          </div>

          {/* Text details */}
          <div className="flex-1">
            <div className="flex flex-wrap gap-2 mb-3">
              {movie.genre.split(',').map((g) => (
                <span key={g} className="bg-[#F5A623]/15 text-[#C47D0A] text-xs font-semibold px-3 py-1 rounded-full">
                  {g.trim()}
                </span>
              ))}
            </div>

            <h1 className="text-3xl font-extrabold text-[#22232B] mb-1">{movie.title}</h1>
            <p className="text-[#6B7280] italic mb-4">Hấp dẫn · kịch tính</p>

            <div className="flex items-center gap-4 mb-5 flex-wrap">
              <div className="flex items-center gap-1">
                <Star className="w-5 h-5 text-[#F5A623] fill-current" />
                <span className="text-xl font-extrabold text-[#22232B]">{movie.rating || 'N/A'}</span>
                <span className="text-sm text-[#6B7280]">/ 10</span>
              </div>
              <div className="flex items-center gap-1 text-sm text-[#6B7280]">
                <Clock className="w-4 h-4" />
                {movie.duration} phút
              </div>
              {movie.releaseDate && (
                <div className="flex items-center gap-1 text-sm text-[#6B7280]">
                  <CalendarIcon className="w-4 h-4" />
                  {movie.releaseDate.split('T')[0]}
                </div>
              )}
            </div>

            <div className="flex gap-3">
              <button
                onClick={() => {
                  // Cuộn xuống phần suất chiếu
                  document.getElementById('booking-section')?.scrollIntoView({ behavior: 'smooth' });
                }}
                className="px-6 py-3 rounded-xl bg-[#F5A623] text-white font-bold hover:bg-[#E09415] transition-all shadow-md shadow-[#F5A623]/20 active:scale-95"
              >
                Đặt vé ngay
              </button>
              {movie.trailerUrl && (
                <button
                  onClick={() => setShowTrailer(true)}
                  className="px-6 py-3 rounded-xl border border-black/10 text-[#22232B] font-semibold hover:bg-black/5 transition-all flex items-center gap-2"
                >
                  <Play className="w-4 h-4" />
                  Trailer
                </button>
              )}
            </div>
          </div>
        </div>

        {/* Tabs for Info */}
        <div className="flex gap-1 mt-10 mb-6 border-b border-black/8">
          {(['info', 'reviews'] as const).map((tab) => {
            const labels = { info: 'Nội dung phim', reviews: 'Đánh giá' };
            return (
              <button
                key={tab}
                onClick={() => setActiveTab(tab)}
                className={`px-5 py-3 text-sm font-semibold transition-all border-b-2 -mb-px ${
                  activeTab === tab
                    ? 'text-[#F5A623] border-[#F5A623]'
                    : 'text-[#6B7280] border-transparent hover:text-[#22232B]'
                }`}
              >
                {labels[tab]}
              </button>
            );
          })}
        </div>

        {activeTab === 'info' ? (
          <div className="space-y-8">
            <p className="text-[#22232B] leading-relaxed text-sm">{movie.synopsis || 'Phim chưa có tóm tắt chi tiết.'}</p>
            <div className="grid grid-cols-2 gap-4 text-sm">
              <div className="bg-white rounded-2xl p-4 border border-black/5">
                <div className="text-[#6B7280] mb-1">Đạo diễn</div>
                <div className="font-semibold text-[#22232B]">{movie.director || 'Chưa cập nhật'}</div>
              </div>
              <div className="bg-white rounded-2xl p-4 border border-black/5">
                <div className="text-[#6B7280] mb-1">Thời lượng</div>
                <div className="font-semibold text-[#22232B]">{movie.duration} phút</div>
              </div>
            </div>
          </div>
        ) : (
          <div className="space-y-4">
            <div className="bg-white rounded-2xl p-5 border border-black/5 text-center text-[#6B7280] text-sm">
              Chưa có đánh giá nào cho phim này. Hãy đặt vé và chia sẻ cảm nhận của bạn!
            </div>
          </div>
        )}

        {/* Booking & Showtimes Selection */}
        <div id="booking-section" className="mt-12 pt-8 border-t border-black/5">
          <h2 className="text-xl font-bold text-[#22232B] mb-5">Chọn lịch chiếu</h2>

          {/* Date Selector */}
          <div className="flex gap-3 mb-6 overflow-x-auto pb-2">
            {dates.map((dateStr) => {
              const { weekday, label } = formatDateLabel(dateStr);
              const isActive = selectedDate === dateStr;
              return (
                <button
                  key={dateStr}
                  onClick={() => setSelectedDate(dateStr)}
                  className={`flex flex-col items-center justify-center w-16 h-20 rounded-2xl border transition-all shrink-0 ${
                    isActive
                      ? 'bg-[#F5A623] border-[#F5A623] text-white shadow-lg shadow-[#F5A623]/20'
                      : 'bg-white border-black/10 text-[#22232B] hover:border-black/20'
                  }`}
                >
                  <span className={`text-[10px] uppercase font-semibold ${isActive ? 'text-white/80' : 'text-[#6B7280]'}`}>
                    {weekday}
                  </span>
                  <span className="text-lg font-bold mt-1">
                    {label.split('/')[0]}
                  </span>
                  <span className={`text-[10px] ${isActive ? 'text-white/80' : 'text-[#6B7280]'}`}>
                    Thg {label.split('/')[1]}
                  </span>
                </button>
              );
            })}
          </div>

          {/* Showtime branch groups */}
          <div className="space-y-6">
            {branchGroups.length > 0 ? (
              branchGroups.map((group) => (
                <div key={group.branchName} className="bg-white border border-black/5 rounded-2xl p-5">
                  <h3 className="font-bold text-sm text-[#22232B] mb-4 flex items-center gap-2">
                    <span className="w-2.5 h-2.5 rounded-full bg-[#F5A623]" />
                    {group.branchName}
                  </h3>
                  <div className="flex flex-wrap gap-3">
                    {group.showtimes.map((st) => (
                      <Link
                        key={st.id}
                        href={`/booking/${st.id}`}
                        className="group relative px-5 py-3 rounded-xl border border-black/10 hover:border-[#F5A623] hover:shadow-md transition-all bg-[#FAFAFA] hover:bg-white text-center"
                      >
                        <div className="font-bold text-[#22232B] text-lg">{formatShowTime(st.startTime)}</div>
                        <div className={`text-[10px] font-semibold mt-0.5 px-2 py-0.5 rounded-full inline-block ${formatColor(st.format)}`}>
                          {st.format}
                        </div>
                        <div className="text-[10px] text-[#6B7280] mt-1">
                          {st.roomName}
                        </div>
                      </Link>
                    ))}
                  </div>
                </div>
              ))
            ) : (
              <div className="text-center p-8 bg-white border border-black/5 rounded-2xl text-sm text-[#6B7280]">
                Rất tiếc, không có suất chiếu nào cho ngày này. Vui lòng chọn ngày khác.
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Trailer modal */}
      {showTrailer && movie.trailerUrl && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm">
          <div className="relative bg-[#1C1C22] rounded-2xl overflow-hidden w-full max-w-2xl mx-4">
            <div className="aspect-video bg-black">
              <iframe
                title={`${movie.title} Trailer`}
                src={movie.trailerUrl}
                className="w-full h-full"
                allowFullScreen
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
              />
            </div>
            <button
              onClick={() => setShowTrailer(false)}
              className="absolute top-3 right-3 w-8 h-8 rounded-full bg-white/10 flex items-center justify-center text-white hover:bg-white/20 transition-colors"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
