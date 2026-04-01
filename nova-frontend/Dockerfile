FROM node:18-alpine AS builder

WORKDIR /app

# 为了更快地构建和避免权限问题，使用 pnpm
RUN npm install -g pnpm

COPY package.json pnpm-lock.yaml* ./
# 尝试使用 locked file，如果尚未生成则普通安装
RUN pnpm install --frozen-lockfile || pnpm install

COPY . .
RUN pnpm run build

# 第二阶段：Nginx
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
