FROM node:20-alpine

WORKDIR /app

# Sao chép package.json và cài đặt dependencies
COPY package*.json ./
RUN npm install

# Sao chép toàn bộ mã nguồn
COPY . .

# Build ứng dụng Next.js sang chế độ Production
RUN npm run build

# Mở cổng 3000 cho frontend
EXPOSE 3000

# Khởi chạy Next.js Server
CMD ["npm", "start"]
