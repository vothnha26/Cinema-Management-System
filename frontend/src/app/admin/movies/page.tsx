'use client';

import { useState, useEffect, useRef } from 'react';
import { Film, Plus, Edit, Trash, X, Save, Search, RefreshCw, Star, Play, Sparkles, Image as ImageIcon, Globe, ShieldAlert } from 'lucide-react';
import { adminMovieService, MovieRequestData } from '../../../services/admin';
import { useAuthStore } from '../../../store/authStore';
import api from '../../../config/api';
import { hasPermission } from '../../../utils/rbac';

const STATUS_BADGES = {
  SHOWING: 'bg-green-50 text-green-700 border-green-200',
  PRE_RELEASE: 'bg-amber-50 text-amber-700 border-amber-200',
  COMING: 'bg-blue-50 text-blue-700 border-blue-200',
  STOPPED: 'bg-red-50 text-red-700 border-red-200',
};

const AGE_RATINGS = ['P', 'K', 'T13', 'T16', 'T18'];

export default function AdminMoviesPage() {
  const { user } = useAuthStore();
  const isAdmin = hasPermission(user?.role, 'MANAGE_USERS');

  const [movies, setMovies] = useState<any[]>([]);
  const [filteredMovies, setFilteredMovies] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');

  // Lọc và tìm kiếm
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  // Phân trang
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 8;

  // Trạng thái modal
  const [showModal, setShowModal] = useState(false);
  const [editId, setEditId] = useState<number | null>(null);
  const [viewOnly, setViewOnly] = useState(false);

  // Form Fields
  const [title, setTitle] = useState('');
  const [status, setStatus] = useState<'COMING' | 'PRE_RELEASE' | 'SHOWING' | 'STOPPED'>('COMING');
  const [description, setDescription] = useState('');
  const [originCountry, setOriginCountry] = useState('');
  const [rating, setRating] = useState<number>(0);
  const [duration, setDuration] = useState<number>(120);
  const [releaseDate, setReleaseDate] = useState('');
  const [ageRating, setAgeRating] = useState<string>('P');
  const [priorityLevel, setPriorityLevel] = useState<number>(1);
  const [trailerUrl, setTrailerUrl] = useState('');
  const [genres, setGenres] = useState('');
  const [director, setDirector] = useState('');
  const [actors, setActors] = useState('');

  // Các trường ẩn TMDB avatar
  const [directorAvatarUrl, setDirectorAvatarUrl] = useState('');
  const [actorAvatarUrls, setActorAvatarUrls] = useState('');

  // File Upload & Preview
  const [posterFile, setPosterFile] = useState<File | null>(null);
  const [posterPreview, setPosterPreview] = useState('');
  const [hiddenPosterUrl, setHiddenPosterUrl] = useState('');
  const fileInputRef = useRef<HTMLInputElement>(null);

  // TMDB integration
  const [tmdbQuery, setTmdbQuery] = useState('');
  const [tmdbResults, setTmdbResults] = useState<any[]>([]);
  const [searchingTmdb, setSearchingTmdb] = useState(false);

  const fetchMovies = async () => {
    setLoading(true);
    setErrorMsg('');
    try {
      let res;
      const isManager = hasPermission(user?.role, 'ACCESS_ADMIN_DASHBOARD') && !hasPermission(user?.role, 'MANAGE_USERS');
      if (isManager && (user as any).branchId) {
        // Manager xem phim của chi nhánh mình quản lý
        res = await api.get(`/movies/branch/${(user as any).branchId}`);
      } else {
        // Admin xem toàn bộ
        res = await api.get('/movies');
      }

      if (res.data?.success) {
        setMovies(res.data.data || []);
      } else {
        setErrorMsg('Không thể tải danh sách phim!');
      }
    } catch (err: any) {
      console.error(err);
      setErrorMsg(err.response?.data?.message || 'Có lỗi xảy ra khi tải phim.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user) {
      fetchMovies();
    }
  }, [user]);

  // Bộ lọc
  useEffect(() => {
    let result = [...movies];
    if (searchTerm.trim()) {
      const q = searchTerm.toLowerCase();
      result = result.filter(
        (m) =>
          m.title?.toLowerCase().includes(q) ||
          m.originCountry?.toLowerCase().includes(q) ||
          m.genres?.some((g: string) => g.toLowerCase().includes(q))
      );
    }
    if (statusFilter) {
      result = result.filter((m) => m.status === statusFilter);
    }
    setFilteredMovies(result);
    setCurrentPage(1);
  }, [movies, searchTerm, statusFilter]);

  // Auto suggest status dựa vào Release Date
  const handleReleaseDateChange = (dateVal: string) => {
    setReleaseDate(dateVal);
    if (!dateVal) return;
    const releaseDateObj = new Date(dateVal);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const diffTime = releaseDateObj.getTime() - today.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays > 7) setStatus('COMING');
    else if (diffDays > 0) setStatus('PRE_RELEASE');
    else setStatus('SHOWING');
  };

  // Open Modals
  const handleOpenCreateModal = () => {
    setEditId(null);
    setViewOnly(false);
    setTitle('');
    setStatus('COMING');
    setDescription('');
    setOriginCountry('');
    setRating(0);
    setDuration(120);
    setReleaseDate('');
    setAgeRating('P');
    setPriorityLevel(1);
    setTrailerUrl('');
    setGenres('');
    setDirector('');
    setActors('');
    setDirectorAvatarUrl('');
    setActorAvatarUrls('');
    setPosterFile(null);
    setPosterPreview('');
    setHiddenPosterUrl('');
    setTmdbQuery('');
    setTmdbResults([]);
    setShowModal(true);
  };

  const handleOpenEditModal = (movie: any, viewMode = false) => {
    setEditId(movie.id);
    setViewOnly(viewMode);
    setTitle(movie.title || '');
    setStatus(movie.status || 'COMING');
    setDescription(movie.description || '');
    setOriginCountry(movie.originCountry || '');
    setRating(movie.rating || 0);
    setDuration(movie.duration || 120);
    setReleaseDate(movie.releaseDate ? movie.releaseDate.split('T')[0] : '');
    setAgeRating(movie.ageRating || 'P');
    setPriorityLevel(movie.priorityLevel || 1);
    setTrailerUrl(movie.trailerUrl || '');
    setGenres(Array.isArray(movie.genres) ? movie.genres.join(', ') : movie.genres || '');
    setDirector(Array.isArray(movie.directors) ? movie.directors.map((d: any) => d.name).join(', ') : movie.director || '');
    setActors(Array.isArray(movie.actors) ? movie.actors.map((a: any) => a.name).join(', ') : movie.actors || '');

    setDirectorAvatarUrl(movie.directorAvatarUrl || '');
    setActorAvatarUrls(movie.actorAvatarUrls || '');

    setPosterFile(null);
    setPosterPreview(movie.posterUrl || '');
    setHiddenPosterUrl(movie.posterUrl || '');
    setTmdbQuery('');
    setTmdbResults([]);
    setShowModal(true);
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setPosterFile(file);
      const reader = new FileReader();
      reader.onload = (event) => {
        setPosterPreview(event.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  // Tìm kiếm TMDB
  const handleSearchTMDB = async () => {
    if (!tmdbQuery.trim()) return;
    setSearchingTmdb(true);
    try {
      const res = await api.get(`/tmdb/search?query=${encodeURIComponent(tmdbQuery)}`);
      setTmdbResults(res.data?.results || []);
    } catch (err) {
      console.error('Lỗi tìm TMDB:', err);
    } finally {
      setSearchingTmdb(false);
    }
  };

  // Đồng bộ TMDB detail
  const handleImportTMDB = async (tmdbId: number) => {
    setTmdbResults([]);
    try {
      const res = await api.get(`/tmdb/movie/${tmdbId}`);
      const m = res.data;
      if (m) {
        // Tìm quốc gia
        let country = 'N/A';
        if (m.production_countries && m.production_countries.length > 0) {
          country = m.production_countries[0].name;
        } else if (m.origin_country && m.origin_country.length > 0) {
          country = m.origin_country[0];
        } else {
          country = m.original_language || 'N/A';
        }

        setTitle(m.title || '');
        setDescription(m.overview || '');
        setDuration(m.runtime || 120);
        setReleaseDate(m.release_date || '');
        setRating(m.vote_average || 0);
        setOriginCountry(country);
        setHiddenPosterUrl(m.poster_path ? `https://image.tmdb.org/t/p/w500${m.poster_path}` : '');
        setPosterPreview(m.poster_path ? `https://image.tmdb.org/t/p/w500${m.poster_path}` : '');

        // Trích xuất đạo diễn, diễn viên
        if (m.credits) {
          const ds = (m.credits.crew || []).filter((c: any) => c.job === 'Director');
          const as = (m.credits.cast || []).slice(0, 10);
          setDirector(ds.map((d: any) => d.name).join(', '));
          setDirectorAvatarUrl(ds.map((d: any) => d.profile_path).join(','));
          setActors(as.map((a: any) => a.name).join(', '));
          setActorAvatarUrls(as.map((a: any) => a.profile_path).join(','));
        }

        // Suggest trạng thái từ ngày phát hành
        if (m.release_date) {
          handleReleaseDateChange(m.release_date);
        }
      }
    } catch (err) {
      console.error('Lỗi chi tiết TMDB:', err);
    }
  };

  const handleSaveMovie = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!isAdmin) {
      // Logic MANAGER chỉnh sửa độ ưu tiên xếp lịch phim tại chi nhánh
      if (editId !== null && (user as any)?.branchId) {
        try {
          const res = await api.patch(
            `/movies/branch/${(user as any).branchId}/movie/${editId}/priority?priority=${priorityLevel}`
          );
          if (res.data?.success) {
            setShowModal(false);
            fetchMovies();
          } else {
            alert(res.data?.message || 'Cập nhật độ ưu tiên thất bại!');
          }
        } catch (err: any) {
          alert(err.response?.data?.message || 'Lỗi khi cập nhật ưu tiên.');
        }
      }
      return;
    }

    // ADMIN xử lý lưu toàn diện phim
    const movieData: any = {
      title,
      status,
      description,
      originCountry,
      rating: Number(rating),
      duration: Number(duration),
      releaseDate: releaseDate ? `${releaseDate}T00:00:00` : undefined,
      ageRating,
      priorityLevel: Number(priorityLevel),
      trailerUrl,
      genres: genres
        ? genres
            .split(',')
            .map((g) => g.trim())
            .filter(Boolean)
        : [],
      director,
      actors,
      directorAvatarUrl,
      actorAvatarUrls,
      posterUrl: hiddenPosterUrl,
    };

    const multipartFd = new FormData();
    if (posterFile) {
      multipartFd.append('poster', posterFile);
    }
    multipartFd.append('movie', new Blob([JSON.stringify(movieData)], { type: 'application/json' }));

    try {
      let res;
      if (editId !== null) {
        // multipart update
        res = await api.put(`/movies/${editId}`, multipartFd, {
          headers: { 'Content-Type': 'multipart/form-data' },
        });
      } else {
        // multipart create
        res = await api.post('/movies', multipartFd, {
          headers: { 'Content-Type': 'multipart/form-data' },
        });
      }

      if (res.data?.success) {
        setShowModal(false);
        fetchMovies();
        alert('Lưu thông tin phim thành công!');
      } else {
        alert(res.data?.message || 'Lưu phim thất bại!');
      }
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Lỗi xảy ra khi lưu phim.');
    }
  };

  const handleDeleteMovie = async (id: number) => {
    if (!confirm('Bạn có chắc chắn muốn xóa vĩnh viễn phim này khỏi hệ thống?')) return;
    try {
      const res = await adminMovieService.deleteMovie(id);
      if (res?.success) {
        fetchMovies();
      } else {
        alert(res?.message || 'Xóa phim thất bại!');
      }
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Lỗi xảy ra khi xóa phim.');
    }
  };

  // Phân trang tính toán
  const totalPages = Math.ceil(filteredMovies.length / pageSize);
  const paginatedMovies = filteredMovies.slice((currentPage - 1) * pageSize, currentPage * pageSize);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-[#22232B] flex items-center gap-2">
            <Film className="w-6 h-6 text-[#F5A623]" />
            Quản lý Kho Phim
          </h1>
          <p className="text-sm text-[#6B7280]">
            {!isAdmin
              ? 'Tùy chỉnh độ ưu tiên xếp lịch chiếu cho phim tại chi nhánh'
              : 'Thêm phim mới, upload Poster và cấu hình phân phối rạp phim'}
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={fetchMovies}
            className="p-2.5 bg-white border border-black/10 text-[#6B7280] rounded-xl hover:bg-black/5 transition-colors"
            title="Làm mới"
          >
            <RefreshCw className="w-4 h-4" />
          </button>

          {isAdmin && (
            <button
              onClick={handleOpenCreateModal}
              className="px-5 py-2.5 bg-[#F5A623] text-white rounded-xl font-bold hover:bg-[#E09415] transition-all flex items-center gap-2 shadow-md shadow-[#F5A623]/25 active:scale-[0.98]"
            >
              <Plus className="w-5 h-5" />
              Thêm phim mới
            </button>
          )}
        </div>
      </div>

      {errorMsg && (
        <div className="bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-xl">
          {errorMsg}
        </div>
      )}

      {/* Bộ lọc tìm kiếm */}
      <div className="flex flex-col sm:flex-row gap-4 items-center bg-white p-4 border border-black/5 rounded-2xl shadow-sm">
        <div className="relative flex-grow w-full">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-[#6B7280]" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Tìm tên phim, quốc gia, thể loại..."
            className="w-full pl-10 pr-4 py-2.5 border border-black/10 rounded-xl bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B]"
          />
        </div>

        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="w-full sm:w-48 px-3 py-2.5 border border-black/10 rounded-xl text-sm font-semibold bg-white text-[#22232B] outline-none"
        >
          <option value="">Tất cả trạng thái</option>
          <option value="SHOWING">Đang chiếu (Showing)</option>
          <option value="PRE_RELEASE">Chiếu sớm (Sneak)</option>
          <option value="COMING">Sắp chiếu (Coming)</option>
          <option value="STOPPED">Ngưng chiếu (Stopped)</option>
        </select>
      </div>

      {/* Lưới Phim */}
      <div className="bg-white border border-black/5 rounded-2xl overflow-hidden shadow-sm">
        {loading ? (
          <div className="p-12 text-center text-[#6B7280] font-medium">Đang tải danh sách phim...</div>
        ) : filteredMovies.length === 0 ? (
          <div className="p-12 text-center text-[#6B7280] font-medium">Không tìm thấy phim nào phù hợp.</div>
        ) : (
          <div>
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-[#FAFAFA] border-b border-black/5">
                    <th className="px-6 py-4 text-xs font-bold text-[#6B7280] uppercase tracking-wider">Thông tin phim</th>
                    <th className="px-6 py-4 text-xs font-bold text-[#6B7280] uppercase tracking-wider">Thời lượng</th>
                    <th className="px-6 py-4 text-xs font-bold text-[#6B7280] uppercase tracking-wider">Đánh giá</th>
                    <th className="px-6 py-4 text-xs font-bold text-[#6B7280] uppercase tracking-wider">Trạng thái</th>
                    <th className="px-6 py-4 text-xs font-bold text-[#6B7280] uppercase tracking-wider text-right">Thao tác</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-black/5">
                  {paginatedMovies.map((m) => (
                    <tr key={m.id} className="hover:bg-black/[0.01] transition-colors">
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          <img
                            src={m.posterUrl || 'https://placehold.co/100x150'}
                            className="w-12 h-18 rounded-lg object-cover bg-gray-100 shadow border border-black/5"
                            alt={m.title}
                          />
                          <div>
                            <div className="font-bold text-[#22232B]">{m.title}</div>
                            <div className="text-xs text-[#6B7280] flex items-center gap-1.5 mt-0.5">
                              <Globe className="w-3.5 h-3.5 text-[#F5A623]" />
                              {(m.originCountry || 'N/A').toUpperCase()} • {m.ageRating}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <span className="font-semibold text-[#6B7280] text-sm">{m.duration} phút</span>
                      </td>
                      <td className="px-6 py-4">
                        <span className="text-amber-500 font-bold flex items-center gap-1 text-sm">
                          <Star className="w-4 h-4 fill-amber-500 text-amber-500" />
                          {m.rating ? m.rating.toFixed(1) : '0.0'}
                        </span>
                      </td>
                      <td className="px-6 py-4">
                        <span
                          className={`inline-flex px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase border ${
                            STATUS_BADGES[m.status as keyof typeof STATUS_BADGES] || ''
                          }`}
                        >
                          {m.status ? m.status.replace('_', ' ') : 'N/A'}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-right">
                        <div className="flex items-center justify-end gap-2">
                          <button
                            onClick={() => handleOpenEditModal(m, true)}
                            className="w-9 h-9 rounded-xl border border-black/10 flex items-center justify-center text-[#6B7280] hover:text-blue-500 hover:border-blue-500 bg-white"
                            title="Xem chi tiết"
                          >
                            <Play className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => handleOpenEditModal(m, false)}
                            className="w-9 h-9 rounded-xl border border-black/10 flex items-center justify-center text-[#6B7280] hover:text-[#F5A623] hover:border-[#F5A623] bg-white"
                            title={isAdmin ? 'Chỉnh sửa phim' : 'Sửa mức ưu tiên'}
                          >
                            <Edit className="w-4 h-4" />
                          </button>
                          {isAdmin && (
                            <button
                              onClick={() => handleDeleteMovie(m.id)}
                              className="w-9 h-9 rounded-xl border border-black/10 flex items-center justify-center text-[#EF4444] hover:bg-red-500/5 hover:border-red-200 bg-white"
                              title="Xóa phim"
                            >
                              <Trash className="w-4 h-4" />
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Phân trang UI */}
            {totalPages > 1 && (
              <div className="flex items-center justify-between p-4 border-t border-black/5 bg-[#FAFAFA]">
                <span className="text-xs text-[#6B7280] font-medium">
                  Hiển thị {(currentPage - 1) * pageSize + 1} - {Math.min(currentPage * pageSize, filteredMovies.length)} của {filteredMovies.length} phim
                </span>

                <div className="flex items-center gap-1.5">
                  {Array.from({ length: totalPages }).map((_, i) => (
                    <button
                      key={i}
                      onClick={() => setCurrentPage(i + 1)}
                      className={`w-8 h-8 rounded-lg text-xs font-bold transition-all ${
                        currentPage === i + 1
                          ? 'bg-[#F5A623] text-white shadow-sm'
                          : 'bg-white border border-black/10 text-[#6B7280] hover:bg-black/5'
                      }`}
                    >
                      {i + 1}
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Modal CRUD Phim */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl max-w-4xl w-full shadow-2xl border border-black/5 overflow-hidden animate-in fade-in zoom-in-95 duration-200">
            {/* Modal Header */}
            <div className="flex items-center justify-between px-6 py-4 border-b border-black/5 bg-[#FAFAFA]">
              <h3 className="font-bold text-lg text-[#22232B]">
                {viewOnly ? 'Chi tiết phim' : editId !== null ? 'Chỉnh sửa thông tin phim' : 'Thêm phim mới'}
              </h3>
              <button
                onClick={() => setShowModal(false)}
                className="w-8 h-8 rounded-full hover:bg-black/5 flex items-center justify-center text-[#6B7280] transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Modal Body */}
            <form onSubmit={handleSaveMovie}>
              <div className="p-6 space-y-4 max-h-[70vh] overflow-y-auto">
                {/* TMDB API search bar (chỉ dành cho ADMIN khi thêm mới hoặc chỉnh sửa và không ở chế độ viewOnly) */}
                {isAdmin && !viewOnly && (
                  <div className="p-4 bg-blue-50/50 border border-blue-200/60 rounded-2xl space-y-3">
                    <label className="block text-xs font-bold text-blue-700 uppercase tracking-wider flex items-center gap-1">
                      <Sparkles className="w-4 h-4 text-[#F5A623]" /> Đồng bộ dữ liệu tự động từ TMDB API
                    </label>
                    <div className="flex gap-2">
                      <input
                        type="text"
                        value={tmdbQuery}
                        onChange={(e) => setTmdbQuery(e.target.value)}
                        placeholder="Nhập tên phim tiếng Anh hoặc tiếng Việt để tìm..."
                        className="flex-grow px-3 py-2 text-sm border border-black/10 rounded-xl bg-white text-[#22232B]"
                        onKeyDown={(e) => {
                          if (e.key === 'Enter') {
                            e.preventDefault();
                            handleSearchTMDB();
                          }
                        }}
                      />
                      <button
                        type="button"
                        onClick={handleSearchTMDB}
                        className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-xs font-bold shadow-sm flex items-center gap-1.5"
                      >
                        {searchingTmdb ? 'Đang tìm...' : 'Tìm TMDB'}
                      </button>
                    </div>

                    {/* Kết quả TMDB */}
                    {tmdbResults.length > 0 && (
                      <div className="border border-black/5 rounded-xl bg-white overflow-hidden max-h-48 overflow-y-auto divide-y divide-black/5 shadow-inner">
                        {tmdbResults.slice(0, 6).map((item) => (
                          <div
                            key={item.id}
                            onClick={() => handleImportTMDB(item.id)}
                            className="flex items-center gap-3 p-2 hover:bg-black/[0.02] cursor-pointer transition-colors"
                          >
                            <img
                              src={item.poster_path ? `https://image.tmdb.org/t/p/w92${item.poster_path}` : 'https://placehold.co/40x60'}
                              className="w-10 h-15 rounded object-cover shadow border border-black/5"
                              alt={item.title}
                            />
                            <div>
                              <div className="font-bold text-xs text-[#22232B]">{item.title}</div>
                              <div className="text-[10px] text-[#6B7280] font-semibold">
                                {item.release_date ? item.release_date.split('-')[0] : 'N/A'} • {(item.original_language || '').toUpperCase()}
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                )}

                {/* Phân hệ Manager edit / Admin edit */}
                {!isAdmin ? (
                  <div className="max-w-md mx-auto p-4 border border-black/5 rounded-2xl bg-[#FAFAFA] text-center space-y-4">
                    <h4 className="font-extrabold text-[#F5A623]">{title}</h4>
                    <div className="max-w-xs mx-auto">
                      <label className="block text-xs font-bold text-[#6B7280] uppercase tracking-wider mb-2">
                        Độ ưu tiên xếp lịch tại chi nhánh (1 - 10)
                      </label>
                      <input
                        type="number"
                        min="1"
                        max="10"
                        value={priorityLevel}
                        onChange={(e) => setPriorityLevel(Number(e.target.value))}
                        className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white font-extrabold text-center text-lg focus:border-[#F5A623] text-[#22232B]"
                      />
                    </div>
                  </div>
                ) : (
                  /* ADMIN FULL FORM */
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    {/* Poster upload column */}
                    <div className="space-y-4">
                      <label className="block text-xs font-bold text-[#6B7280] uppercase tracking-wider">Poster Phim</label>
                      <div
                        onClick={() => !viewOnly && fileInputRef.current?.click()}
                        className={`w-full aspect-[2/3] border-2 border-dashed rounded-2xl flex flex-col items-center justify-center cursor-pointer overflow-hidden relative group bg-[#FAFAFA] transition-all hover:bg-black/[0.02] ${
                          viewOnly ? 'pointer-events-none' : 'border-black/15 hover:border-[#F5A623]'
                        }`}
                      >
                        {posterPreview ? (
                          <img src={posterPreview} className="w-full h-full object-cover" alt="Poster Preview" />
                        ) : (
                          <div className="text-center p-4">
                            <ImageIcon className="w-10 h-10 text-[#6B7280] mx-auto mb-2" />
                            <span className="text-xs text-[#6B7280] font-semibold">Tải ảnh lên</span>
                          </div>
                        )}
                        {!viewOnly && posterPreview && (
                          <div className="absolute inset-0 bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                            <span className="text-white text-xs font-bold">Đổi hình ảnh</span>
                          </div>
                        )}
                      </div>
                      <input
                        type="file"
                        ref={fileInputRef}
                        onChange={handleFileChange}
                        accept="image/*"
                        className="hidden"
                      />

                      <div>
                        <label className="block text-[10px] font-bold text-[#6B7280] uppercase tracking-wider mb-1">
                          Poster URL (Nếu có)
                        </label>
                        <input
                          type="text"
                          disabled={viewOnly}
                          value={hiddenPosterUrl}
                          onChange={(e) => {
                            setHiddenPosterUrl(e.target.value);
                            setPosterPreview(e.target.value);
                          }}
                          placeholder="https://..."
                          className="w-full px-3 py-2 border border-black/10 rounded-xl text-xs bg-white text-[#22232B] outline-none"
                        />
                      </div>
                    </div>

                    {/* Movie info columns */}
                    <div className="md:col-span-2 space-y-4">
                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        <div className="sm:col-span-2">
                          <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Tiêu đề phim *</label>
                          <input
                            type="text"
                            required
                            disabled={viewOnly}
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            placeholder="VD: Avengers: Endgame"
                            className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B]"
                          />
                        </div>

                        <div>
                          <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Trạng thái phát hành</label>
                          <select
                            disabled={viewOnly}
                            value={status}
                            onChange={(e) => setStatus(e.target.value as any)}
                            className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B] font-semibold"
                          >
                            <option value="COMING">Sắp chiếu (COMING)</option>
                            <option value="PRE_RELEASE">Chiếu sớm (PRE_RELEASE)</option>
                            <option value="SHOWING">Đang chiếu (SHOWING)</option>
                            <option value="STOPPED">Ngưng chiếu (STOPPED)</option>
                          </select>
                        </div>

                        <div>
                          <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Giới hạn độ tuổi</label>
                          <select
                            disabled={viewOnly}
                            value={ageRating}
                            onChange={(e) => setAgeRating(e.target.value)}
                            className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B] font-semibold"
                          >
                            {AGE_RATINGS.map((a) => (
                              <option key={a} value={a}>
                                Nhãn {a}
                              </option>
                            ))}
                          </select>
                        </div>

                        <div className="sm:col-span-2">
                          <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Tóm tắt cốt truyện</label>
                          <textarea
                            disabled={viewOnly}
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            rows={3}
                            placeholder="Mô tả tóm tắt nội dung phim..."
                            className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B]"
                          />
                        </div>

                        <div>
                          <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Quốc gia sản xuất</label>
                          <input
                            type="text"
                            disabled={viewOnly}
                            value={originCountry}
                            onChange={(e) => setOriginCountry(e.target.value)}
                            placeholder="VD: United States, Vietnam..."
                            className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B]"
                          />
                        </div>

                        <div>
                          <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Độ ưu tiên AI (1-10)</label>
                          <input
                            type="number"
                            disabled={viewOnly}
                            value={priorityLevel}
                            onChange={(e) => setPriorityLevel(Number(e.target.value))}
                            className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B]"
                          />
                        </div>

                        <div>
                          <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Thời lượng (phút)</label>
                          <input
                            type="number"
                            required
                            disabled={viewOnly}
                            value={duration}
                            onChange={(e) => setDuration(Number(e.target.value))}
                            className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B]"
                          />
                        </div>

                        <div>
                          <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Điểm đánh giá hệ thống</label>
                          <input
                            type="number"
                            step="0.1"
                            disabled={viewOnly}
                            value={rating}
                            onChange={(e) => setRating(Number(e.target.value))}
                            className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B]"
                          />
                        </div>

                        <div>
                          <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Ngày phát hành</label>
                          <input
                            type="date"
                            disabled={viewOnly}
                            value={releaseDate}
                            onChange={(e) => handleReleaseDateChange(e.target.value)}
                            className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B]"
                          />
                        </div>

                        <div>
                          <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Thể loại (phân tách bởi dấu phẩy)</label>
                          <input
                            type="text"
                            disabled={viewOnly}
                            value={genres}
                            onChange={(e) => setGenres(e.target.value)}
                            placeholder="Hành động, Phiêu lưu, Viễn tưởng"
                            className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B]"
                          />
                        </div>

                        <div className="sm:col-span-2">
                          <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Trailer Youtube (Embed Link)</label>
                          <input
                            type="text"
                            disabled={viewOnly}
                            value={trailerUrl}
                            onChange={(e) => setTrailerUrl(e.target.value)}
                            placeholder="https://www.youtube.com/embed/..."
                            className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B]"
                          />
                        </div>

                        <div>
                          <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Đạo diễn</label>
                          <input
                            type="text"
                            disabled={viewOnly}
                            value={director}
                            onChange={(e) => setDirector(e.target.value)}
                            className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B]"
                          />
                        </div>

                        <div>
                          <label className="block text-sm font-semibold text-[#22232B] mb-1.5">Diễn viên chính</label>
                          <input
                            type="text"
                            disabled={viewOnly}
                            value={actors}
                            onChange={(e) => setActors(e.target.value)}
                            className="w-full px-4 py-2.5 rounded-xl border border-black/10 bg-white text-sm outline-none focus:border-[#F5A623] text-[#22232B]"
                          />
                        </div>
                      </div>
                    </div>
                  </div>
                )}
              </div>

              {/* Modal Footer */}
              <div className="px-6 py-4 bg-[#FAFAFA] border-t border-black/5 flex items-center justify-between">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-5 py-2.5 border border-black/10 rounded-xl font-semibold text-[#6B7280] hover:bg-black/5 text-sm transition-all"
                >
                  Hủy bỏ
                </button>

                {!viewOnly && (
                  <button
                    type="submit"
                    className="px-5 py-2.5 bg-[#F5A623] text-white rounded-xl font-bold hover:bg-[#E09415] transition-all flex items-center gap-2 shadow-md shadow-[#F5A623]/25 active:scale-[0.98] text-sm"
                  >
                    <Save className="w-4 h-4" />
                    Lưu dữ liệu
                  </button>
                )}
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
