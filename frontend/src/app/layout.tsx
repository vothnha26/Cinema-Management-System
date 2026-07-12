import type { Metadata } from 'next';
import '../styles/globals.css';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';

export const metadata: Metadata = {
  title: 'StarCinema Elite - Hệ Thống Đặt Vé Xem Phim Trực Tuyến',
  description: 'Đặt vé xem phim trực tuyến nhanh chóng, tiện lợi với trải nghiệm rạp chiếu phim sang trọng, đẳng cấp hàng đầu Việt Nam.',
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="vi" suppressHydrationWarning>
      <body className="antialiased font-sans min-h-screen flex flex-col bg-[#FAFAFA]">
        <Navbar />
        <main className="flex-grow pt-16">
          {children}
        </main>
        <Footer />
      </body>
    </html>
  );
}
