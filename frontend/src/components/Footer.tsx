import Link from 'next/link';

export default function Footer() {
  return (
    <footer className="bg-[#1C1C22] text-white">
      <div className="max-w-7xl mx-auto px-6 py-12">
        <div className="grid grid-cols-1 sm:grid-cols-4 gap-8">
          <div>
            <div className="flex items-center gap-2 mb-4">
              <div className="w-8 h-8 rounded-lg bg-[#F5A623] flex items-center justify-center">
                <span className="text-white text-sm font-bold">S</span>
              </div>
              <span className="font-bold text-lg">StarCinema</span>
            </div>
            <p className="text-sm text-white/60 leading-relaxed">
              Hệ thống đặt vé xem phim trực tuyến hiện đại và nhanh nhất Việt Nam.
            </p>
          </div>
          {[
            { title: 'Khám phá', links: [{ name: 'Phim đang chiếu', path: '/' }, { name: 'Sắp chiếu', path: '/' }, { name: 'Ưu đãi & Khuyến mãi', path: '/' }] },
            { title: 'Hỗ trợ', links: [{ name: 'Câu hỏi thường gặp', path: '/' }, { name: 'Liên hệ', path: '/' }, { name: 'Chính sách hoàn tiền', path: '/' }] },
            { title: 'Kết nối', links: [{ name: 'Facebook', path: '/' }, { name: 'Instagram', path: '/' }, { name: 'YouTube', path: '/' }] },
          ].map((col) => (
            <div key={col.title}>
              <h4 className="font-semibold mb-4">{col.title}</h4>
              <ul className="space-y-2">
                {col.links.map((link) => (
                  <li key={link.name}>
                    <Link href={link.path} className="text-sm text-white/60 hover:text-white transition-colors">
                      {link.name}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
        <div className="mt-10 pt-6 border-t border-white/10 text-center text-xs text-white/40">
          © {new Date().getFullYear()} StarCinema. Thiết kế và phát triển bởi StarCinema.
        </div>
      </div>
    </footer>
  );
}
