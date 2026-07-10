# RASICK-EX Technical Report

---

## SECTION 1 - PROJECT OVERVIEW

### Application Type
This is a hybrid music streaming / media library web application with both a React frontend and an Express/MongoDB backend. It is built as a single-page application for web browsers with a separate backend API server.

### Purpose
The application is intended to host, browse, search, stream, download, and manage audio tracks. It includes a user-facing music discovery experience and an admin panel for uploading songs and managing categories.

### Main Workflow
1. User visits the web app.
2. User browses categories and sections like Trending, Latest, Recommended, and specific category pages.
3. User searches songs by text or voice.
4. User plays audio through the browser audio player.
5. User may download songs for offline playback.
6. Admin logs in separately and uploads cover images plus audio files, manages categories, edits/deletes songs.

### Technologies Used
- Frontend:
  - React 19
  - TypeScript
  - Vite
  - @tanstack/react-router
  - @tanstack/react-query
  - Tailwind CSS
  - Radix UI components
  - Lucide icons
- Backend:
  - Node.js
  - Express
  - MongoDB via Mongoose
  - Multer for file uploads
  - bcryptjs for password hashing
  - dotenv
  - cors
- Storage:
  - Local filesystem for uploads
  - IndexedDB/localStorage for offline downloads/likes/recent playback
- Audio:
  - HTMLAudioElement playback via custom browser playback service

### Current Project Maturity Level
- Prototype / early production readiness
- Core user flows are implemented but many features are incomplete, insecure, or inconsistent
- Not yet production-ready due to authentication, security, scalability, and browser compatibility gaps

---

## SECTION 2 - COMPLETE PROJECT ARCHITECTURE

### High-Level Architecture
```
User Browser
   ↓
React SPA
   ↓
Frontend API Client
   ↓
Backend API (Express)
   ↓
MongoDB
   ↓
Local Upload Storage
```

### Detailed Architecture Diagram

```
Frontend
 ├─ React SPA
 │   ├─ Pages: Home, Search, Library, Downloads, Category, Login, Admin
 │   ├─ Components: AppShell, BottomNav, MiniPlayer, FullPlayer, SongCard
 │   ├─ Context: AuthContext, PlayerContext
 │   └─ Offline Storage: IndexedDB + localStorage

Backend
 ├─ Express server
 │   ├─ Routes: /api/auth, /api/songs, /api/categories, /api/admin, /api/artists
 │   ├─ Middleware: CORS, JSON parsing, auth, error handling
 │   ├─ Upload handling: Multer, local disk storage
 │   └─ Static serving: /uploads

Database
 ├─ MongoDB collections
 │   ├─ users
 │   ├─ songs
 │   ├─ categories
 │   └─ artists
```

### Module Architecture
- Admin
  - Upload Song
  - Category management
  - Song edit/delete
  - Dashboard metrics
- Upload
  - Multer file filter
  - Audio + cover storage
- Backend API
  - Authentication
  - Song CRUD
  - Category CRUD
  - Artist read
- Streaming
  - HTMLAudioElement source set to `/uploads/audio/<file>`
  - `Accept-Ranges: bytes` on audio static files
- Search
  - Backend text search on title, artist, category
  - Frontend search page with voice-triggered input
- Voice Search
  - Browser Web Speech API
  - UI fallback if unsupported
- Storage
  - Local file storage for uploads
  - IndexedDB for downloads
  - localStorage for likes/recent history

---

## SECTION 3 - COMPLETE FOLDER STRUCTURE

### Top-Level
- `package.json`
  - Frontend dependency definitions and scripts
- `bunfig.toml`
  - Bun settings if used
- `tsconfig.json`
  - TypeScript config
- `vite.config.ts`
  - Vite dev server config
- `src/`
  - React app source

### `backend/`
- `package.json`
  - Backend dependencies and scripts
- `server.js`
  - Express server startup
- `config/`
  - `db.js` — MongoDB connection
  - `seed.js` — initial data seeding
  - `upload.js` — Multer upload config and supported audio types
  - `formatters.js` — response formatting
- `controllers/`
  - `authController.js` — login
  - `songController.js` — songs CRUD
  - `categoryController.js` — category CRUD
  - `adminController.js` — dashboard metrics
  - `artistController.js` — artist list
- `middleware/`
  - `auth.js` — header-based auth
  - `errorHandler.js` — centralized error handling
- `models/`
  - `User.js`, `Song.js`, `Category.js`, `Artist.js`
- `routes/`
  - `index.js`
  - `authRoutes.js`
  - `songRoutes.js`
  - `categoryRoutes.js`
  - `adminRoutes.js`
  - `artistRoutes.js`
- `uploads/`
  - `audio/`
  - `covers/`
- `tests/`
  - `upload.test.js`
- `scripts/`
  - `integration-test.js`

### `src/`
- `router.tsx`
  - React router creation
- `routeTree.gen.ts`
  - route tree definitions
- `server.ts`
  - server entry for SSR/React Start
- `start.ts`
  - likely app bootstrap
- `styles.css`
  - app styles
- `assets/`
  - logo asset
- `components/`
  - app layout and reusable UI components
- `contexts/`
  - `auth-context.tsx`
  - `player-context.tsx`
- `hooks/`
  - `use-mobile.tsx`
  - `use-voice-search.ts`
- `lib/`
  - `api.ts` — API client
  - `config.server.ts`
  - `error-capture.ts`
  - `error-page.ts`
  - `lovable-error-reporting.ts`
  - `mock-data.ts`
  - `playback-service.ts`
  - `user-library.ts` — offline library storage
  - `utils.ts`
  - `api/` — possible sub-API modules
- `routes/`
  - Main pages and admin pages

### Folder Purposes
- `src/components`: UI building blocks
- `src/routes`: app screens and page routing
- `src/lib`: API bindings, client utilities, playback and library persistence
- `src/contexts`: app state providers
- `backend/config`: backend runtime config and upload rules
- `backend/controllers`: business logic per route
- `backend/middleware`: request-level logic
- `backend/models`: DB schema objects
- `backend/routes`: route registration
- `backend/uploads`: file storage targets

---

## SECTION 4 - FRONTEND ANALYSIS

### React Components
- `SongCard` — plays song, shows title/artist
- `MiniPlayer` — collapsed player control
- `FullPlayer` — expanded playback UI with seek, like, download
- `BottomNav` — main navigation
- `AppShell` — layout wrapper for pages
- Many Radix-based UI primitives under `components/ui`

### Pages
- `home.tsx` — category browsing
- `search.tsx` — search + voice search
- `downloads.tsx` — offline downloads
- `library.tsx` — library shortcuts
- `library/liked.tsx` — liked songs
- `library/recent.tsx` — recently played
- `category.$name.tsx` — category contents
- `login-selection.tsx` — choose admin/user login
- `login/admin.tsx` — admin login
- `login/user.tsx` — user login
- `admin/upload.tsx` — upload interface
- `admin/dashboard.tsx` — admin panel
- `admin/categories.tsx` — category management

### Layouts
- AppShell provides shared page layout, but there is no explicit master route layout besides page wrappers.
- `BottomNav` and player components overlay the UI.

### Routes
- React Router powered by `@tanstack/react-router`
- Route tree defined in `routeTree.gen.ts`
- Primary browser routes:
  - `/home`, `/search`, `/downloads`, `/library`, `/category/$name`
  - `/login-selection`, `/login/admin`, `/login/user`
  - `/admin/dashboard`, `/admin/upload`, `/admin/categories`

### Navigation
- bottom nav for main pages
- links in home/search/category/library pages
- admin menu only appears for admin role
- navigation uses `useNavigate` and route links

### State Management
- Auth via `AuthContext`
- Playback via `PlayerContext`
- Local state per page for UI form values
- `useState`, `useEffect`, `useCallback`
- no global state library besides context and React state

### API Calls
- Centralized in `src/lib/api.ts`
- uses browser `fetch`
- CRUD endpoints for songs/categories/auth
- `getAuthHeaders` sends `x-username` and `x-user-role`
- `resolveMediaUrl` rewrites relative media paths

### Search
- Search query triggers backend call with debounce
- Backend search supports case-insensitive regex over title, artist, and category
- Search page also performs local category filtering from category list

### Voice Search
- Implemented in `useVoiceSearch`
- Uses browser SpeechRecognition/Web Speech API
- UI shows support status and listens if available
- Fallback shows browser unsupported message

### Downloads
- Download button saves audio to IndexedDB via `user-library.ts`
- Songs are kept in localStorage download metadata
- Downloaded songs appear in `/downloads`
- Songs can be cleared or removed

### Streaming Player
- Browser playback via HTMLAudioElement wrapper in `playback-service.ts`
- Player context loads either cached offline audio or online audio
- Mini/full player support UI controls, seek, play/pause, next/prev

### Authentication UI
- Login pages for admin and user
- Form submission uses `loginApi`
- Auth state stored in sessionStorage
- Admin-only routes guarded in UI with `useAuth`

### Admin UI
- Dashboard with song list and quick actions
- Upload page with cover and audio file selection
- Category management page with create/edit/delete
- Admin navigation available only if authenticated as admin

### Completed
- Basic browsing UI
- Search UI
- Voice search UI
- Playback and player controls
- Downloads UI
- Admin upload and category management
- Local offline storage support

### Missing / Needs Improvement
- No universal route protection on frontend beyond simple `isAdmin` checks
- Some UI pages lack consistent fallback/loading states
- No pagination or lazy loading for large song lists
- Search results are shallow and only filtered server-side on query
- No playlist or queue persistence beyond current session
- No explicit user profile page beyond login-storage
- No theme switch or accessibility optimizations described
- No advanced audio controls (equalizer, bitrate, formats)

---

## SECTION 5 - BACKEND ANALYSIS

### Node.js / Express
- Backend is Express app in `backend/server.js`
- Starts on `process.env.PORT || 5000`
- Uses `cors` with origin allow list, JSON parsing, URL-encoded parsing
- Serves static `/uploads` folder with Accept-Ranges on audio files

### Controllers
- `authController.login`
- `songController.getSongs`, `createSong`, `updateSong`, `deleteSong`
- `categoryController.getCategories`, `createCategory`, `updateCategory`, `deleteCategory`
- `adminController.getDashboard`
- `artistController.getArtists`

### Routes
- `/api/auth/login`
- `/api/songs`
  - GET list/search
  - POST create (admin)
  - PUT update (admin)
  - DELETE delete (admin)
- `/api/categories`
  - GET list
  - POST create (admin)
  - PUT update (admin)
  - DELETE delete (admin)
- `/api/admin/dashboard`
- `/api/artists`

### Middleware
- `auth.js`
  - Authentication by request headers `x-username`, `x-user-role`
  - Role validation for admin-only actions
- `errorHandler.js`
  - Multipurpose HTTP error response formatting
  - Handles multer errors, validation, duplicates, cast errors

### Authentication
- Login validates username/password/role against MongoDB
- No JWT or session cookies are used
- Backend auth protection is only header-based
- Admin routes require valid header combination and DB user
- Login response returns only username + role

### Database Logic
- MongoDB connection through `backend/config/db.js`
- Seed data inserts default admin/user and sample categories, artists, songs
- Song filtering by section, category, and query regex

### Streaming Logic
- Audio streaming is static file serving from `/uploads/audio`
- `Accept-Ranges` header allows byte-range requests
- No HLS/DASH or adaptive streaming
- Playback is browser-managed from direct audio URLs

### Upload Logic
- Uses Multer disk storage
- Accepts cover files in `uploads/covers`
- Accepts audio files in `uploads/audio`
- Validates `.mp3`, `.dts`, `.ac3` by extension or MIME type
- Maximum upload size 50 MB

### Download Logic
- Downloads are served via existing audio URLs
- Frontend uses `fetch(song.audioUrl)` and caches blob locally
- Backend does not provide a special download endpoint

### Search Logic
- Song model has full-text index on title, artist, category
- GET `/api/songs?q=` performs regex search across title, artist, category
- Category filter and section filter are supported

### Category Logic
- Categories stored in MongoDB
- Category CRUD updates songs if category names change
- Category delete unsets song category values

### Voice Search Logic
- Entirely frontend/browser based
- No backend voice recognition or transcription
- Browser compatibility dependent

### APIs
- Complete set of backend APIs is:
  - `POST /api/auth/login`
  - `GET /api/songs`
  - `POST /api/songs`
  - `PUT /api/songs/:id`
  - `DELETE /api/songs/:id`
  - `GET /api/categories`
  - `POST /api/categories`
  - `PUT /api/categories/:id`
  - `DELETE /api/categories/:id`
  - `GET /api/admin/dashboard`
  - `GET /api/artists`

---

## SECTION 6 - DATABASE ANALYSIS

### Collections
- `users`
- `songs`
- `categories`
- `artists`

### Relationships
- `Song.uploadedBy` references `User`
- Categories are stored as string names on songs, not foreign keys
- No explicit many-to-many relations
- Likes/recent/downloads are not stored in DB

### Schemas
- `Song`
  - title, artist, cover, duration, category, section, audioUrl, uploadedBy, isActive
  - text index on title, artist, category
- `User`
  - username, password, role
  - unique compound index on username+role
- `Category`
  - name, color, songCount
- `Artist`
  - name, image

### Indexes
- `Song`:
  - `{ section: 1, createdAt: -1 }`
  - `{ category: 1 }`
  - `{ title: "text", artist: "text", category: "text" }`
- `User`: `{ username: 1, role: 1 }` unique
- `Category`: unique name
- `Artist`: unique name

### Current Data Flow
- `seed.js` prepopulates users, categories, artists, sample songs
- `Song.getSongs` queries DB and returns formatted results
- `Category.getCategories` returns categories and aggregates song counts
- `createSong` stores song metadata and file paths
- No analytics or download tracking is persisted

### Mentioned Entities
- Songs: core data stored in MongoDB with file URLs and text metadata
- Users: login credentials + roles
- Categories: browse and grouping metadata
- Downloads: frontend-only local cache, not DB
- Likes: frontend-only localStorage
- Recently Played: frontend-only localStorage

---

## SECTION 7 - STORAGE ANALYSIS

### Current Storage Implementation
- Backend file storage is local disk under `backend/uploads/`
- Audio files go to `backend/uploads/audio`
- Covers go to `backend/uploads/covers`
- Uploaded file names are UUID + extension
- Static files are exposed via Express `/uploads`

### Streaming
- Browser loads audio directly from backend static URLs
- Static files served with range support
- No CDN or cloud object store

### Downloads
- Offline downloads are fetched via song audio URL
- Audio blob stored in IndexedDB
- Download metadata stored in localStorage
- No backend involvement once downloaded

### Offline Playback
- Uses IndexedDB cached blob URL
- `PlayerContext` prefers cached offline audio source when available
- Downloaded audio is available only on the same browser/device

### Storage Type
- Local (backend file system + browser storage)
- Not cloud
- Not hybrid

### Scalability
- Current architecture is not scalable for production
  - Local disk storage cannot scale to multiple backend instances
  - No cloud storage / CDN
  - No backup or replication policy
  - MongoDB is external but only minimally configured

---

## SECTION 8 - AUTHENTICATION ANALYSIS

### Current Login System
- Login endpoint checks MongoDB username/password/role
- Frontend stores logged-in user in `sessionStorage`
- Auth headers are `x-username` and `x-user-role`

### JWT / Sessions
- No JWT implementation
- No server-side sessions
- No access/refresh tokens

### Roles
- Roles: `admin`, `user`
- Admin-specific routes and UI restricted by role
- Backend `requireAdmin` middleware checks role from authenticated user

### Admin Access
- Admin check is header-based
- True admin access is only as secure as header values and DB lookup
- No password or token is persisted on responses

### User Access
- User login active, but normal user routes are not strongly enforced
- Authentication is only required for admin actions and some endpoints

### Security Level
- Low to moderate
- Vulnerabilities:
  - Header-based auth can be spoofed if requests are sent manually
  - No token-based authentication
  - No HTTPS enforcement or CSRF protection
  - Session state in browser is not cryptographically protected
  - No password reset or account management

### Improvements
- Add JWTs or session cookies
- Remove header-only auth and use real tokens
- Add secure password flow
- Enforce HTTPS and CORS more strictly
- Add role-based authorization for all sensitive endpoints
- Add account registration and proper user session management

---

## SECTION 9 - AUDIO SYSTEM ANALYSIS

### Upload System
- Admin upload form accepts `.mp3`, `.dts`, `.ac3` by extension
- Backend upload filter supports `.mp3`, `.dts`, `.ac3`
- Backend also checks MIME types including `audio/vnd.dts`, `audio/ac3`, etc.
- File size cap 50 MB

### Streaming System
- Browser playback is direct audio source from the API server
- No transcoding on the backend
- No adaptive streaming
- Browser must support direct playback of the format

### Player
- Uses HTMLAudioElement
- Handles play/pause, seek, next, prev
- Plays cached blob or online file
- No advanced format negotiation

### Supported Formats
- Explicitly supported: MP3, DTS, AC3
- UI says "MP3, WAV, AAC, FLAC supported" but backend only accepts MP3/DTS/AC3
- WAV/AAC/FLAC are falsely advertised in UI

### Why Unsupported Formats Fail
- Browser support for `.dts` and `.ac3` is extremely limited
- Most desktop/mobile browsers do not decode DTS/AC3 directly
- HTMLAudioElement cannot play unsupported codecs even if file is served
- If browser returns `MEDIA_ERR_SRC_NOT_SUPPORTED`, playback fails

### Browser Limitations
- Chrome / Firefox / Safari generally do not support DTS/AC3 playback
- Support is platform-dependent and often absent
- Audio streaming of these formats in the browser is not reliable
- Files may still download but not play

### Android Limitations
- Android browser support for DTS/AC3 is also limited
- Native Android TV may support them only through platform hardware/OS decoder
- Web apps are not a good way to guarantee DTS/AC3 playback

---

## SECTION 10 - CURRENT PROJECT STATUS

Completion status by major area:

- UI: Partially Completed
- Backend: Partially Completed
- Database: Partially Completed
- Authentication: Partial / insecure
- Streaming: Partial
- Downloads: Partial
- Offline Playback: Partial
- Voice Search: Partial
- Categories: Partial
- Search: Partial
- Admin: Partial

Summary:
- Completed: core browsing and admin CRUD
- Partially Completed: login, upload, playback, downloads
- Not Started: production-grade security, scalable storage, robust format compatibility, true Android/TV support

---

## SECTION 11 - BUG ANALYSIS

### Bugs Found
1. **Auth spoofing**: backend accepts `x-username` / `x-user-role` headers as authentication.
   - Root cause: no token/session enforcement.
2. **UI misleading audio support**: upload UI claims WAV/AAC/FLAC supported, but backend rejects them.
   - Root cause: UI text mismatch backend validation.
3. **Search category matching**: search page filters categories locally, but uses backend only for songs; UX may be inconsistent.
4. **Offline library inconsistency**: downloaded metadata and cached blob are separate; removing blob may not always sync if storage fails.
5. **Category delete**: updates song category to empty string but does not remove `category` field cleanly.
6. **Song update**: only updates selected fields, but cover/audio replacement not supported in PUT.
7. **Admin page metrics**: uses hardcoded "Downloads" and "Users" values from UI, not actual DB metrics.
8. **Voice search fallback**: message is shown but no alternative flow beyond browser compatibility.
9. **Potential memory leak**: `URL.createObjectURL` may not be revoked consistently after caching or preview generation.
10. **Audio format mismatch**: `.dts` and `.ac3` may upload, but browser playback likely fails silently.

### Incomplete Features
- Proper secure login/session management
- Real user registration/profiles
- True audio format compatibility for DTS/AC3
- Scalable upload storage
- Multi-user data persistence for likes/downloads/recent
- Production error monitoring and logging
- Hosting/deployment config

### Security Issues
- No JWT or secure auth tokens
- Header-based identity spoofing
- No HTTPS/CSP enforcement at server
- CORS allowed broad host patterns
- No rate limiting or brute-force protection

### UI Issues
- Some pages show empty state but no loaders
- Admin route guard can flash UI before redirect
- Search uses `window.alert` for errors
- Upload form messaging is inconsistent with backend
- Mobile/responsive behavior unknown but probably basic

### Backend Issues
- No global auth token or session
- Local disk storage is not safe for horizontal scaling
- No upload cleanup on failed DB or storage mismatch beyond file deletion
- No dedicated download endpoint for secure access
- No API versioning

### Database Issues
- Many denormalized fields (category as string)
- No persisted likes/downloads/recent playback
- Seed data is sample-only and not admin user configurable
- No audit trail for uploads or deletes

### Storage Issues
- Local filesystem only
- Not suitable for multiple server instances
- No backup/replication plan
- No cloud/object store or CDN

---

## SECTION 12 - PERFORMANCE ANALYSIS

### Performance
- Frontend likely performs well for small data sets
- React UI is mostly client-side and light
- `fetchSongs` requests may return full lists, no pagination
- Search debounce is implemented but no server-side paging

### Memory Usage
- Player uses browser audio element; cached blobs may increase memory if not cleaned
- IndexedDB store may grow without quota management
- LocalStorage may hold significant song metadata if user likes/downloads many songs

### API Efficiency
- Backend search uses regex, which can be expensive on large collections
- `Category.getCategories` does aggregation each request
- No caching layer
- `Song.getSongs` returns all matched songs with no limit

### Streaming Efficiency
- Single-file direct streaming is simple but not optimized
- No range support beyond static file headers; it is adequate for browser playback but not optimal for many simultaneous users
- No compression or media transcoding

### Database Efficiency
- Text indices exist for song search
- Category and section indexes help filtering
- Search regex on large collections may still scan indexes poorly
- No compound index combining text search and category/section filters

### Backend Performance
- Express middleware stack is light
- MongoDB connection uses default options
- No profiler, monitoring, or load testing
- Static files served by Express, not optimized CDN

### Improvements
- Add pagination and limit on song queries
- Use full-text search or search engine instead of regex
- Add caching for categories and popular song lists
- Move static file serving to CDN or Nginx
- Add rate limiting and request throttling
- Reduce client bundle size if needed

---

## SECTION 13 - DEPLOYMENT STATUS

### Current Deployment
- No deployment manifests found
- Backend and frontend are local dev builds only

### Can other users access?
- Only if local machine is exposed and backend is running
- No hosted environment appears configured

### Can multiple users access?
- The backend can technically serve multiple requests
- Session/auth design is not multi-user safe due to header spoofing
- User-specific data is not isolated for library metadata

### Hosting Status
- Not deployed
- No Docker, cloud config, or hosting pipeline present

### Production Readiness
- Not production-ready
- Needs secure auth, scalable storage, deployment config, and monitoring

### Environment Variables
- `MONGODB_URI` required for backend
- `CLIENT_URL` optional for CORS
- `VITE_API_URL` optional for frontend API target

---

## SECTION 14 - FEATURE CHECKLIST

| Feature | Status |
|---|---|
| Admin Login | Partial |
| User Login | Partial |
| Upload Songs | Partial |
| Delete Songs | Partial |
| Edit Songs | Partial |
| Categories | Partial |
| Search | Partial |
| Voice Search | Partial |
| Streaming | Partial |
| Downloads | Partial |
| Offline Playback | Partial |
| Library | Partial |
| Recently Played | Partial |
| Liked Songs | Partial |
| Android TV | Missing |
| APK | Missing |
| Hosting | Missing |
| JWT | Missing |
| Cloud Storage | Missing |
| DTS | Partial |
| AC3 | Partial |

---

## SECTION 15 - ROADMAP

### Phase 1 — Stabilize Core System
1. Implement secure authentication with JWT or cookies
2. Replace header auth with token-based authorization
3. Harden Express with HTTPS/CSP and rate limiting
4. Fix upload UI/backend format support discrepancies
5. Add proper route protection / auth guards

### Phase 2 — Improve Data Persistence
1. Persist likes/downloads/recent activity in backend per user
2. Add user registration/profile management
3. Add proper DB audit logs for uploads and deletes
4. Implement pagination for song queries
5. Add backend download endpoint with auth

### Phase 3 — Scale Storage and Streaming
1. Replace local disk uploads with cloud/object storage
2. Configure CDN/static asset hosting
3. Add media metadata extraction and validation
4. Improve search with a dedicated search index or service
5. Create deployment manifests / Docker / hosting pipeline

### Phase 4 — Audio Compatibility & Android Support
1. Add native support strategy for DTS/AC3 without conversion
2. Implement fallback for unsupported browser codecs
3. Build a native Android/TV client or wrapper
4. Support HDMI passthrough through native playback path
5. Test cross-platform audio playback thoroughly

### Difficulty and Priority
- High priority: secure auth, storage scalability, JWT, route protection
- Medium priority: persistent user data, search optimization, upload flow refinement
- Lower priority: Android TV, native playback, advanced audio codec support

### Estimated Effort
- Phase 1: 2–4 weeks
- Phase 2: 3–5 weeks
- Phase 3: 4–6 weeks
- Phase 4: 4–8 weeks

---

## SECTION 16 - DTS / AC3 ANALYSIS

### 1. Current upload system
- Backend already accepts `.dts` and `.ac3`
- Audio field filter checks file extension or MIME type
- Upload writes files to local disk under `backend/uploads/audio`

### 2. Why DTS and AC3 uploads fail
- Likely failure is not backend rejection, but browser playback support
- The upload file picker may accept files, backend accepts them, but HTMLAudioElement cannot decode them
- Browser `audio` element may produce `MEDIA_ERR_SRC_NOT_SUPPORTED`

### 3. Whether frontend file picker blocks them
- Frontend accepts `.mp3,.dts,.ac3,audio/*`
- It also performs `isSupportedAudioFile` extension check for `.mp3,.dts,.ac3`
- So the picker should not block `.dts` and `.ac3`

### 4. Whether backend validation blocks them
- Backend validation allows `.dts` and `.ac3`
- MIME support includes DTS and AC3-related types
- So backend validation should not be the blocker in most cases

### 5. Required MIME types
- Possible accepted MIME types:
  - `audio/x-dts`
  - `audio/vnd.dts`
  - `audio/vnd.dts.hd`
  - `audio/ac3`
  - `audio/x-ac3`
  - `audio/vnd.dolby.dd-raw`
  - `application/octet-stream` fallback for many file systems
- `audio/mpeg`, `audio/mp3` for MP3

### 6. Required upload configuration
- Keep Multer fileFilter logic for `.dts` and `.ac3`
- Accept browser-provided MIME types and fallback on extension
- Ensure upload size limit is sufficient
- Add explicit support for file selection attributes and perhaps accept multiple audio codecs

### 7. Streaming support
- Backend can stream raw DTS/AC3 files if served with correct headers
- Browser HTMLAudio may still not decode
- For true streaming support, use a native client/Android app or server-side transcoding if browser-based playback is required

### 8. Browser limitations
- Browsers generally do not natively decode DTS/AC3
- Even if the URL is valid, Chrome/Firefox may fail
- There is no reliable browser-based playback path for these codecs
- The only safe path is native OS-level playback or container with supported codecs

### 9. Android TV playback possibilities
- Android TV native media player may support DTS/AC3 if device hardware/OS decoder is present
- Web app on Android TV is not guaranteed
- Native Android app has better chance than browser web app

### 10. HDMI passthrough architecture
- For DTS/AC3 passthrough, the player must hand raw frames to the Android/TV OS audio sink
- The web audio stack cannot provide HDMI passthrough
- Native media playback pipeline is required

### 11. Native Android player requirements
- Use Android `MediaPlayer` / `ExoPlayer`
- Ensure platform supports DTS/AC3 and passthrough
- Use `MediaExtractor` and pass file URI directly
- Provide playback via service and UI similar to web player

### 12. Exact implementation plan without converting originals
1. Keep backend upload support for `.dts` and `.ac3`
2. Expose raw file URL for stored audio
3. Build a native Android/TV app or wrapper
4. Use native media playback APIs on Android/TV
5. If web playback is required, provide a format support detection step and fail gracefully
6. Optionally support direct download to native app for local playback
7. Avoid browser playback of DTS/AC3 entirely; only use browser for MP3 or supported codecs
8. For web, present downloadable links and instruct the user to use native app/player
9. If you need web playback, implement server-side passthrough or packaging into browser-supported containers only for preview, while keeping originals intact

---

## SECTION 17 - FINAL PROJECT SCORE

| Metric | Score (0–10) |
|---|---|
| Architecture | 5 |
| UI | 6 |
| Backend | 5 |
| Database | 5 |
| Security | 3 |
| Performance | 5 |
| Scalability | 3 |
| Production Readiness | 3 |
| Android Compatibility | 2 |
| Android TV Compatibility | 1 |
| Streaming | 5 |
| Storage | 4 |
| Code Quality | 6 |
| Maintainability | 5 |

### Overall Completion Percentage
- Estimated: **45%**

### Next 10 Prioritized Tasks
1. Implement secure JWT / session-based authentication and remove header-only auth
2. Add backend authorization middleware for all protected endpoints
3. Correct frontend upload UI format support and backend validation alignment
4. Replace local filesystem uploads with cloud/object storage or S3-compatible storage
5. Add pagination/limit logic to song fetching and search
6. Persist likes/downloads/recent playback per user in backend
7. Harden backend with rate limiting, HTTPS, and CORS security
8. Add production deployment configuration (Docker, hosting, environment management)
9. Build native Android / Android TV playback path for DTS/AC3 support
10. Add monitoring/logging and meaningful error handling for production
