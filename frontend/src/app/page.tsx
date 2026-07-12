'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { Star, ChevronLeft, ChevronRight, Bell, Play, MapPin, Calendar, Filter, ArrowRight } from 'lucide-react';
import { movieService } from '../services/movieService';

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
}

interface Branch {
  id: number;
  name: string;
}

interface Genre {
  id: number;
  name: string;
}

// Dữ liệu fallback tuyệt đẹp khi backend trống hoặc lỗi kết nối
const fallbackNowPlaying: Movie[] = [
  { id: 1, title: 'Dune: Phần Hai', genre: 'Khoa học viễn tưởng', rating: 8.6, duration: 166, posterUrl: 'https://images.unsplash.com/photo-1518676590629-3dcbd9c5a5c9?w=300&h=450&fit=crop&auto=format', backdropUrl: 'https://images.unsplash.com/photo-1518676590629-3dcbd9c5a5c9?w=1200&h=500&fit=crop&auto=format' },
  { id: 2, title: 'Godzilla x Kong', genre: 'Hành động', rating: 7.3, duration: 115, posterUrl: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=300&h=450&fit=crop&auto=format', backdropUrl: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=1200&h=500&fit=crop&auto=format' },
  { id: 3, title: 'Kung Fu Panda 4', genre: 'Hoạt hình', rating: 7.1, duration: 94, posterUrl: 'https://images.unsplash.com/photo-1485846234645-a62644f84728?w=300&h=450&fit=crop&auto=format', backdropUrl: 'https://images.unsplash.com/photo-1485846234645-a62644f84728?w=1200&h=500&fit=crop&auto=format' },
];

const fallbackUpcoming: Movie[] = [
  { id: 4, title: 'Venom: Kèo Cuối', genre: 'Siêu anh hùng', rating: 0, duration: 120, releaseDate: '25-10-2026', posterUrl: 'https://images.unsplash.com/photo-1611532736597-de2d4265fba3?w=300&h=450&fit=crop&auto=format' },
  { id: 5, title: 'Joker: Điên Có Đôi', genre: 'Tội phạm, Nhạc kịch', rating: 0, duration: 138, releaseDate: '04-10-2026', posterUrl: 'https://images.unsplash.com/photo-1542204165-65bf26472b9b?w=300&h=450&fit=crop&auto=format' },
];

export default function HomePage() {
  const [slideIndex, setSlideIndex] = useState(0);
  const [hoveredCard, setHoveredCard] = useState<number | null>(null);
  const [nowPlaying, setNowPlaying] = useState<Movie[]>([]);
  const [upcoming, setUpcoming] = useState<Movie[]>([]);
  const [branches, setBranches] = useState<Branch[]>([]);
  const [genres, setGenres] = useState<Genre[]>([]);
  
  const [selectedBranch, setSelectedBranch] = useState('');
  const [selectedGenre, setSelectedGenre] = useState('');

  useEffect(() => {
    // Gọi API lấy dữ liệu thực tế từ backend
    const fetchData = async () => {
      try {
        const [showingRes, comingRes, branchesRes, genresRes] = await Promise.all([
          movieService.getNowPlaying().catch(() => null),
          movieService.getUpcoming().catch(() => null),
          movieService.getBranches().catch(() => null),
          movieService.getGenres().catch(() => null),
        ]);

        if (showingRes?.data && showingRes.data.length > 0) {
          setNowPlaying(showingRes.data);
        } else {
          setNowPlaying(fallbackNowPlaying);
        }

        if (comingRes?.data && comingRes.data.length > 0) {
          setUpcoming(comingRes.data);
        } else {
          setUpcoming(fallbackUpcoming);
        }

        if (branchesRes?.data) {
          setBranches(branchesRes.data);
        }

        if (genresRes?.data) {
          setGenres(genresRes.data);
        }
      } catch (err) {
        console.error('Lỗi khi tải dữ liệu trang chủ:', err);
        setNowPlaying(fallbackNowPlaying);
        setUpcoming(fallbackUpcoming);
      }
    };

    fetchData();
  }, []);

  const featured = nowPlaying.length > 0 ? nowPlaying.slice(0, 3) : fallbackNowPlaying;
  const current = featured[slideIndex] || fallbackNowPlaying[0];

  const nextSlide = () => setSlideIndex((i) => (i + 1) % featured.length);
  const prevSlide = () => setSlideIndex((i) => (i - 1 + featured.length) % featured.length);

  return (
    <div className="min-h-screen bg-[#FAFAFA] pb-12">
      {/* Hero Banner */}
      <div className="relative w-full h-[520px]">
        {/* Backdrop */}
        <div className="absolute inset-0 overflow-hidden">
          <img
            src={current.backdropUrl || current.posterUrl}
            alt={current.title}
            className="w-full h-full object-cover transition-all duration-700"
          />
          <div className="absolute inset-0 bg-gradient-to-r from-[#1C1C22]/80 via-[#1C1C22]/40 to-transparent" />
          <div className="absolute inset-0 bg-gradient-to-t from-[#FAFAFA] via-transparent to-transparent" />
        </div>

        {/* Content */}
        <div className="relative z-10 max-w-7xl mx-auto px-6 h-full flex items-center pt-16">
          <div className="flex items-end gap-8">
            {/* Poster */}
            <div className="hidden sm:block w-44 rounded-2xl overflow-hidden shadow-2xl shrink-0">
              <img src={current.posterUrl} alt={current.title} className="w-full h-64 object-cover" />
            </div>
            {/* Info */}
            <div className="pb-10">
              <div className="flex items-center gap-2 mb-3">
                <span className="bg-[#F5A623]/20 text-[#C47D0A] text-xs font-semibold px-3 py-1 rounded-full">
                  {current.genre}
                </span>
                <span className="bg-white/10 text-white text-xs font-medium px-3 py-1 rounded-full backdrop-blur-sm">
                  {current.duration} phút
                </span>
              </div>
              <h1 className="text-4xl font-extrabold text-white mb-2 leading-tight">{current.title}</h1>
              <div className="flex items-center gap-1 mb-6">
                <Star className="w-4 h-4 text-[#F5A623] fill-current" />
                <span className="text-white font-bold">{current.rating || 'N/A'}</span>
                <span className="text-white/60 text-sm">/ 10</span>
              </div>
              <div className="flex items-center gap-3">
                <Link
                  href={`/movies/${current.id}`}
                  className="px-6 py-3 rounded-xl bg-[#F5A623] text-white font-bold hover:bg-[#E09415] transition-all shadow-lg shadow-[#F5A623]/30 active:scale-95 text-center"
                >
                  Mua vé ngay
                </Link>
                {current.trailerUrl && (
                  <a
                    href={current.trailerUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="px-6 py-3 rounded-xl bg-white/15 backdrop-blur-sm text-white font-semibold hover:bg-white/25 transition-all flex items-center gap-2"
                  >
                    <Play className="w-4 h-4 fill-current" />
                    Xem trailer
                  </a>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Slide controls */}
        <button
          onClick={prevSlide}
          className="absolute left-4 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-white/20 backdrop-blur-sm text-white hover:bg-white/30 transition-all flex items-center justify-center z-20"
        >
          <ChevronLeft className="w-5 h-5" />
        </button>
        <button
          onClick={nextSlide}
          className="absolute right-4 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-white/20 backdrop-blur-sm text-white hover:bg-white/30 transition-all flex items-center justify-center z-20"
        >
          <ChevronRight className="w-5 h-5" />
        </button>

        {/* Dots */}
        <div className="absolute bottom-16 left-1/2 -translate-x-1/2 flex gap-2 z-20">
          {featured.map((_, i) => (
            <button
              key={i}
              onClick={() => setSlideIndex(i)}
              className={`rounded-full transition-all ${i === slideIndex ? 'w-6 h-2 bg-[#F5A623]' : 'w-2 h-2 bg-white/40'}`}
            />
          ))}
        </div>

        {/* Quick filter bar */}
        <div className="absolute -bottom-6 left-1/2 -translate-x-1/2 w-full max-w-3xl px-6 z-30">
          <div className="bg-white rounded-2xl shadow-lg shadow-black/10 border border-black/5 p-4 flex items-center gap-3">
            <div className="flex items-center gap-2 flex-1">
              <MapPin className="w-4 h-4 text-[#F5A623]" />
              <select
                value={selectedBranch}
                onChange={(e) => setSelectedBranch(e.target.value)}
                className="flex-1 text-sm text-[#22232B] bg-transparent outline-none font-medium cursor-pointer"
              >
                <option value="">Tất cả chi nhánh</option>
                {branches.map((b) => (
                  <option key={b.id} value={b.id}>{b.name}</option>
                ))}
              </select>
            </div>
            <div className="w-px h-8 bg-black/10" />
            <div className="flex items-center gap-2 flex-1">
              <Calendar className="w-4 h-4 text-[#F5A623]" />
              <select className="flex-1 text-sm text-[#22232B] bg-transparent outline-none font-medium cursor-pointer">
                <option>Hôm nay</option>
                <option>Ngày mai</option>
                <option>Cuối tuần</option>
              </select>
            </div>
            <div className="w-px h-8 bg-black/10" />
            <div className="flex items-center gap-2 flex-1">
              <Filter className="w-4 h-4 text-[#F5A623]" />
              <select
                value={selectedGenre}
                onChange={(e) => setSelectedGenre(e.target.value)}
                className="flex-1 text-sm text-[#22232B] bg-transparent outline-none font-medium cursor-pointer"
              >
                <option value="">Tất cả thể loại</option>
                {genres.map((g) => (
                  <option key={g.id} value={g.name}>{g.name}</option>
                ))}
              </select>
            </div>
            <button className="px-5 py-2 rounded-xl bg-[#F5A623] text-white text-sm font-semibold hover:bg-[#E09415] transition-colors shrink-0">
              Tìm kiếm
            </button>
          </div>
        </div>
      </div>

      {/* Now Playing Section */}
      <section id="now-playing" className="max-w-7xl mx-auto px-6 pt-20 pb-12">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h2 className="text-2xl font-extrabold text-[#22232B]">Phim đang chiếu</h2>
            <p className="text-sm text-[#6B7280] mt-1">Đặt vé xem ngay hôm nay</p>
          </div>
          <button className="flex items-center gap-1 text-sm font-semibold text-[#F5A623] hover:gap-2 transition-all">
            Xem tất cả <ArrowRight className="w-4 h-4" />
          </button>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4">
          {nowPlaying.map((movie) => (
            <div
              key={movie.id}
              className="group relative cursor-pointer"
              onMouseEnter={() => setHoveredCard(movie.id)}
              onMouseLeave={() => setHoveredCard(null)}
            >
              <Link href={`/movies/${movie.id}`}>
                <div className="relative rounded-2xl overflow-hidden bg-[#F4F4F5] aspect-[2/3]">
                  <img
                    src={movie.posterUrl}
                    alt={movie.title}
                    className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
                  />
                  {/* Hover overlay */}
                  <div className={`absolute inset-0 bg-[#1C1C22]/70 flex flex-col items-center justify-end pb-4 transition-opacity duration-200 ${hoveredCard === movie.id ? 'opacity-100' : 'opacity-0'}`}>
                    <span className="px-4 py-2 rounded-xl bg-[#F5A623] text-white text-xs font-bold hover:bg-[#E09415] transition-colors">
                      Đặt vé
                    </span>
                  </div>
                </div>
              </Link>
              <div className="mt-3">
                <h3 className="font-semibold text-sm text-[#22232B] truncate">{movie.title}</h3>
                <div className="flex items-center justify-between mt-1">
                  <span className="text-xs text-[#6B7280]">{movie.genre}</span>
                  <div className="flex items-center gap-0.5">
                    <Star className="w-3 h-3 text-[#F5A623] fill-current" />
                    <span className="text-xs font-semibold text-[#22232B]">{movie.rating || 'N/A'}</span>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Upcoming Section */}
      <section id="upcoming" className="max-w-7xl mx-auto px-6 pb-16">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h2 className="text-2xl font-extrabold text-[#22232B]">Phim sắp chiếu</h2>
            <p className="text-sm text-[#6B7280] mt-1">Đăng ký nhận tin để không bỏ lỡ</p>
          </div>
          <button className="flex items-center gap-1 text-sm font-semibold text-[#F5A623] hover:gap-2 transition-all">
            Xem tất cả <ArrowRight className="w-4 h-4" />
          </button>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          {upcoming.map((movie) => (
            <div key={movie.id} className="group cursor-pointer">
              <Link href={`/movies/${movie.id}`}>
                <div className="relative rounded-2xl overflow-hidden bg-[#F4F4F5] aspect-[2/3]">
                  <img
                    src={movie.posterUrl}
                    alt={movie.title}
                    className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
                  />
                  <div className="absolute top-3 left-3">
                    <span className="bg-[#1C1C22]/80 backdrop-blur-sm text-white text-xs font-semibold px-2.5 py-1 rounded-full">
                      Sắp chiếu
                    </span>
                  </div>
                </div>
              </Link>
              <div className="mt-3">
                <h3 className="font-semibold text-sm text-[#22232B] truncate">{movie.title}</h3>
                <div className="flex items-center justify-between mt-1">
                  <span className="text-xs text-[#6B7280]">{movie.genre}</span>
                  <button
                    onClick={(e) => { e.preventDefault(); alert(`Đã đăng ký nhận thông báo cho phim: ${movie.title}`); }}
                    className="flex items-center gap-1 text-xs font-medium text-[#F5A623] hover:text-[#C47D0A] transition-colors"
                  >
                    <Bell className="w-3 h-3" />
                    Nhắc tôi
                  </button>
                </div>
                <div className="mt-1 text-xs text-[#6B7280]">
                  📅 Khởi chiếu: {movie.releaseDate ? movie.releaseDate.split('T')[0] : 'Đang cập nhật'}
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
