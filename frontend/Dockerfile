FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
COPY angular.json tsconfig.json tsconfig.app.json ./
COPY src ./src
COPY public ./public
RUN npm install
RUN npm run build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/letter-gen-frontend/browser /usr/share/nginx/html
EXPOSE 80