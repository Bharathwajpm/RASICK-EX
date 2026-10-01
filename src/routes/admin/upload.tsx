import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useEffect, useRef, useState } from "react";
import { Upload, ImagePlus, FileAudio, ArrowLeft, X, CheckCircle } from "lucide-react";
import { createSong, fetchCategories, fetchArtists, fetchAlbums, type Category, type Artist, type Album } from "@/lib/api";
import { useAuth } from "@/contexts/auth-context";

const ACCEPTED_AUDIO_EXTENSIONS = [".mp3", ".dts", ".ac3", ".wav", ".aac", ".flac"];

const isSupportedAudioFile = (file: File) => {
  const ext = file.name.toLowerCase().slice(file.name.lastIndexOf("."));
  return ACCEPTED_AUDIO_EXTENSIONS.includes(ext);
};

export const Route = createFileRoute("/admin/upload")({
  head: () => ({ meta: [{ title: "Upload Song — RASICK-EX" }] }),
  component: UploadSong,
});

function UploadSong() {
  const { isAdmin } = useAuth();
  const navigate = useNavigate();
  const coverRef = useRef<HTMLInputElement>(null);
  const audioRef = useRef<HTMLInputElement>(null);

  const [title, setTitle] = useState("");
  const [artist, setArtist] = useState("");
  const [albumId, setAlbumId] = useState("");
  const [category, setCategory] = useState("");
  const [categories, setCategories] = useState<Category[]>([]);
  const [artists, setArtists] = useState<Artist[]>([]);
  const [albums, setAlbums] = useState<Album[]>([]);
  const [coverFile, setCoverFile] = useState<File | null>(null);
  const [audioFile, setAudioFile] = useState<File | null>(null);
  const [coverPreview, setCoverPreview] = useState<string | null>(null);
  const [submitted, setSubmitted] = useState(false);
  const [uploading, setUploading] = useState(false);

  useEffect(() => {
    if (!isAdmin) navigate({ to: "/login-selection" });
  }, [isAdmin, navigate]);

  useEffect(() => {
    if (isAdmin) {
      fetchCategories()
        .then(setCategories)
        .catch(() => setCategories([]));
      fetchArtists()
        .then(setArtists)
        .catch(() => setArtists([]));
      fetchAlbums()
        .then(setAlbums)
        .catch(() => setAlbums([]));
    }
  }, [isAdmin]);

  const handleArtistChange = (name: string) => {
    setArtist(name);
    setAlbumId("");
  };

  const selectedArtist = artists.find((a) => a.name === artist);
  const filteredAlbums = artist
    ? albums.filter((al) => al.artistId === selectedArtist?.id)
    : albums;

  const handleCover = (e: React.ChangeEvent<HTMLInputElement>) => {
    const f = e.target.files?.[0];
    if (!f) return;
    setCoverFile(f);
    setCoverPreview(URL.createObjectURL(f));
  };

  const handleAudio = (e: React.ChangeEvent<HTMLInputElement>) => {
    const f = e.target.files?.[0];
    if (!f) return;

    if (!isSupportedAudioFile(f)) {
      e.target.value = "";
      setAudioFile(null);
      window.alert("Please select a valid audio file (.mp3, .dts, .ac3, .wav, .aac, or .flac).");
      return;
    }

    setAudioFile(f);
  };

  const handleUpload = async () => {
    if (!title.trim() || !category || !coverFile || !audioFile) return;
    setUploading(true);
    try {
      const selectedAlbum = albums.find((a) => a.id === albumId);
      await createSong({
        title: title.trim(),
        category,
        coverFile,
        audioFile,
        artist: artist || undefined,
        section: "latest",
        album: selectedAlbum?.title || undefined,
        albumId: albumId || undefined,
      });
      setSubmitted(true);
      setTimeout(() => {
        setSubmitted(false);
        setTitle("");
        setArtist("");
        setAlbumId("");
        setCategory("");
        setCoverFile(null);
        setAudioFile(null);
        setCoverPreview(null);
      }, 2500);
    } catch (err) {
      window.alert(err instanceof Error ? err.message : "Upload failed");
    } finally {
      setUploading(false);
    }
  };

  if (!isAdmin) return null;

  return (
    <main className="relative min-h-screen bg-background pb-10">
      <div
        className="pointer-events-none absolute inset-x-0 top-0 h-64"
        style={{ background: "var(--gradient-glow)", opacity: 0.45 }}
      />

      {/* Header */}
      <header className="relative z-10 flex items-center gap-3 px-5 pt-10">
        <button
          onClick={() => navigate({ to: "/admin/dashboard" })}
          className="flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground transition-colors"
        >
          <ArrowLeft className="h-4 w-4" /> Dashboard
        </button>
      </header>

      <div className="relative z-10 flex flex-col items-center px-5 pt-6">
        <div className="grid h-14 w-14 place-items-center rounded-2xl bg-gradient-primary shadow-glow">
          <Upload className="h-7 w-7 text-primary-foreground" />
        </div>
        <h1 className="mt-4 text-xl font-bold">Upload Song</h1>
        <p className="text-xs text-muted-foreground">Add a new track to the platform</p>
      </div>

      <div className="relative z-10 mt-8 flex flex-col gap-5 px-5">
        {/* Success toast */}
        {submitted && (
          <div className="flex items-center gap-2 rounded-xl border border-emerald-500/40 bg-emerald-500/10 px-4 py-3 text-sm text-emerald-400">
            <CheckCircle className="h-4 w-4" /> Song uploaded successfully!
          </div>
        )}

        {/* Cover image */}
        <div className="flex flex-col gap-1.5">
          <label className="text-xs font-medium text-muted-foreground uppercase tracking-wider">
            Song Cover Image
          </label>
          <button
            onClick={() => coverRef.current?.click()}
            className="glass relative flex h-44 w-full items-center justify-center overflow-hidden rounded-2xl border border-dashed border-border/60 hover:border-primary/50 transition-colors"
          >
            {coverPreview ? (
              <>
                <img src={coverPreview} alt="" className="h-full w-full object-cover" />
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    setCoverFile(null);
                    setCoverPreview(null);
                  }}
                  className="absolute right-2 top-2 grid h-7 w-7 place-items-center rounded-full bg-background/70"
                >
                  <X className="h-3.5 w-3.5" />
                </button>
              </>
            ) : (
              <div className="flex flex-col items-center gap-2 text-muted-foreground">
                <ImagePlus className="h-8 w-8" />
                <span className="text-xs">Tap to select cover image</span>
              </div>
            )}
          </button>
          <input
            ref={coverRef}
            type="file"
            accept="image/*"
            className="hidden"
            onChange={handleCover}
          />
        </div>

        {/* Song title */}
        <div className="flex flex-col gap-1.5">
          <label className="text-xs font-medium text-muted-foreground uppercase tracking-wider">
            Song Title
          </label>
          <div className="glass flex items-center rounded-xl border border-input px-4 py-3 focus-within:border-primary/60 transition-colors">
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Enter song title"
              className="w-full bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none"
            />
          </div>
        </div>

        {/* Artist */}
        <div className="flex flex-col gap-1.5">
          <label className="text-xs font-medium text-muted-foreground uppercase tracking-wider">
            Artist
          </label>
          <div className="glass flex items-center rounded-xl border border-input px-4 py-3 focus-within:border-primary/60 transition-colors">
            <select
              value={artist}
              onChange={(e) => handleArtistChange(e.target.value)}
              className="w-full bg-transparent text-sm text-foreground focus:outline-none appearance-none"
            >
              <option value="" className="bg-card">
                Select an artist (optional)
              </option>
              {artists.map((a) => (
                <option key={a.id} value={a.name} className="bg-card">
                  {a.name}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Album */}
        <div className="flex flex-col gap-1.5">
          <label className="text-xs font-medium text-muted-foreground uppercase tracking-wider">
            Album
          </label>
          <div className="glass flex items-center rounded-xl border border-input px-4 py-3 focus-within:border-primary/60 transition-colors">
            <select
              value={albumId}
              onChange={(e) => setAlbumId(e.target.value)}
              className="w-full bg-transparent text-sm text-foreground focus:outline-none appearance-none"
            >
              <option value="" className="bg-card">
                Select an album (optional)
              </option>
              {filteredAlbums.map((al) => (
                <option key={al.id} value={al.id} className="bg-card">
                  {al.title}{al.artistName ? ` — ${al.artistName}` : ""}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Category */}
        <div className="flex flex-col gap-1.5">
          <label className="text-xs font-medium text-muted-foreground uppercase tracking-wider">
            Category
          </label>
          <div className="glass flex items-center rounded-xl border border-input px-4 py-3 focus-within:border-primary/60 transition-colors">
            <select
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              className="w-full bg-transparent text-sm text-foreground focus:outline-none appearance-none"
            >
              <option value="" className="bg-card">
                Select a category
              </option>
              {categories.map((c) => (
                <option key={c.id} value={c.name} className="bg-card">
                  {c.name}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Audio file */}
        <div className="flex flex-col gap-1.5">
          <label className="text-xs font-medium text-muted-foreground uppercase tracking-wider">
            Audio File
          </label>
          <button
            onClick={() => audioRef.current?.click()}
            className="glass flex items-center gap-3 rounded-xl border border-dashed border-border/60 px-4 py-4 hover:border-primary/50 transition-colors"
          >
            <div className="grid h-10 w-10 place-items-center rounded-lg bg-primary/10">
              <FileAudio className="h-5 w-5 text-primary" />
            </div>
            <div className="text-left">
              <p className="text-sm font-medium">
                {audioFile ? audioFile.name : "Select audio file"}
              </p>
              <p className="text-xs text-muted-foreground">
                {audioFile
                  ? `${(audioFile.size / 1024 / 1024).toFixed(1)} MB`
                  : "MP3, WAV, AAC, FLAC supported"}
              </p>
            </div>
          </button>
          <input
            ref={audioRef}
            type="file"
            accept=".mp3,.dts,.ac3,.wav,.aac,.flac,audio/*"
            className="hidden"
            onChange={handleAudio}
          />
        </div>

        {/* Buttons */}
        <div className="flex gap-3 pt-2">
          <button
            onClick={() => navigate({ to: "/admin/dashboard" })}
            className="flex-1 rounded-full border border-border py-3.5 text-sm font-semibold text-foreground hover:bg-secondary/60 transition-colors"
          >
            Cancel
          </button>
          <button
            onClick={handleUpload}
            disabled={!title.trim() || !category || !coverFile || !audioFile || uploading}
            className="flex-1 rounded-full bg-gradient-primary py-3.5 text-sm font-semibold text-primary-foreground shadow-glow transition-transform hover:scale-[1.02] active:scale-[0.98] disabled:opacity-40"
          >
            Upload
          </button>
        </div>
      </div>
    </main>
  );
}
