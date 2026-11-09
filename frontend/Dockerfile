FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
ARG VITE_API_URL
ARG VITE_SHOW_DEMO_HINT=true
ENV VITE_API_URL=$VITE_API_URL VITE_SHOW_DEMO_HINT=$VITE_SHOW_DEMO_HINT
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf