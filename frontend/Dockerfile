# ==============================================================================================
# BUYWISE FRONTEND — MULTI-STAGE VITE + NGINX DOCKERFILE
# ==============================================================================================
# ARCHITECTURAL DESIGN & CONTAINER SPECIFICATIONS:
#
# 1. WHY NGINX FOR SINGLE PAGE APPLICATIONS (SPAs)?
#    - In development, Vite runs an internal Node.js dev server with Hot Module Replacement (HMR).
#    - In production, serving static HTML/JS/CSS through a Node.js process is an anti-pattern: it consumes
#      ~120MB+ of RAM and introduces Node event-loop overhead.
#    - Multi-stage build compiles TypeScript into optimized static bundles (HTML, JS, CSS gzip), then
#      copies the distribution files to a hardened Nginx Alpine image (~25MB total image size, ~5MB RAM).
#
# 2. DETERMINISTIC DEPENDENCY RESOLUTION:
#    - `npm ci` (Clean Install) is used instead of `npm install`. It strictly adheres to `package-lock.json`
#      checksums, preventing unexpected package version drifts between local and CI environments.
#
# 3. SPA CLIENT-SIDE ROUTING & REVERSE PROXY:
#    - Modern React routers manage URL paths on the client side via HTML5 History API (`pushState`).
#    - Without Nginx configuration, refreshing a route like `/comparisons/1` would cause a 404 Not Found
#      from Nginx.
#    - In `nginx.conf`, `try_files $uri $uri/ /index.html` rewrites all unmatched URI requests back to
#      `index.html`, allowing React Router to seamlessly handle the route.
# ==============================================================================================

# ----------------------------------------------------------------------------------------------
# STAGE 1: TypeScript Compilation & Vite Production Bundling
# ----------------------------------------------------------------------------------------------
FROM node:20-alpine AS build
WORKDIR /app

# Optimize layer caching: Install dependencies first
COPY package.json package-lock.json* ./
RUN npm ci

# Copy application source code and compile production assets
COPY . .
RUN npm run build

# ----------------------------------------------------------------------------------------------
# STAGE 2: Lightweight Production Web Server (Nginx Alpine)
# ----------------------------------------------------------------------------------------------
FROM nginx:1.27-alpine

# Copy built distribution assets to default Nginx web root
COPY --from=build /app/dist /usr/share/nginx/html

# Copy custom Nginx configuration supporting SPA routing and API reverse-proxying
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

# Container health monitoring
HEALTHCHECK --interval=15s --timeout=3s --retries=3 \
  CMD wget --spider -q http://127.0.0.1:80/ || exit 1

# Run Nginx in foreground to prevent immediate container exit
CMD ["nginx", "-g", "daemon off;"]
