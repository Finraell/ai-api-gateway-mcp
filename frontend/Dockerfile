FROM node:20-bookworm-slim AS build
WORKDIR /app

ENV NPM_CONFIG_UPDATE_NOTIFIER=false
ENV NPM_CONFIG_AUDIT=false
ENV NPM_CONFIG_FUND=false

# Pin npm to avoid the npm 10.9.x/10.9.7 "Exit handler never called" failure seen in some Docker builds.
RUN npm install -g npm@10.8.2

COPY package.json .npmrc ./
RUN npm install --include=dev --no-audit --no-fund \
    && test -x node_modules/.bin/tsc \
    && test -x node_modules/.bin/vite

COPY . .
RUN npm run build

FROM nginxinc/nginx-unprivileged:stable-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY docker/default.conf.template /etc/nginx/templates/default.conf.template
EXPOSE 8080
