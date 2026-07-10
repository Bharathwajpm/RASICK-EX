import type { Song } from "@/lib/mock-data";

export type PlaybackEventHandlers = {
  onTimeUpdate?: (currentTime: number) => void;
  onDurationChange?: (duration: number) => void;
  onPlayStateChange?: (isPlaying: boolean) => void;
  onEnded?: () => void;
  onError?: (error: Error) => void;
};

export interface PlaybackService {
  setSource(source: string | null): void;
  play(): Promise<void>;
  pause(): void;
  seek(time: number): void;
  getCurrentTime(): number;
  getDuration(): number;
  isPaused(): boolean;
  setEventHandlers(handlers: PlaybackEventHandlers): () => void;
  dispose(): void;
  unlock?(): void;
  getCurrentSource(): string | null;
  resolveSource(song: Song): string | null;
}

export function createBrowserPlaybackService(): PlaybackService {
  return new BrowserPlaybackService();
}

class BrowserPlaybackService implements PlaybackService {
  private audio: HTMLAudioElement | null = null;
  private handlers: PlaybackEventHandlers = {};
  private currentSource: string | null = null;
  private isChangingSource = false;
  private transitionTimeout: any = null;

  private log(func: string, line: number, extra?: any) {
    if (!import.meta.env.DEV) return;
    const ts = new Date().toISOString();
    console.log(
      `[LOG] [${ts}] File: playback-service.ts, Func: ${func}, Line: ${line}, ` +
      `Song ID: ${this.currentSource ? this.currentSource.split('/').pop() : 'none'}, ` +
      `audio.src: "${this.audio?.src ?? ''}", isChangingSource: ${this.isChangingSource}, ` +
      `extra: ${JSON.stringify(extra ?? {})}`
    );
  }

  private trace(funcName: string) {
    if (typeof window === "undefined") return;
    (window as any).__traceStep = ((window as any).__traceStep || 0) + 1;
    const step = (window as any).__traceStep;
    const ts = performance.now().toFixed(2);
    const src = this.audio ? this.audio.src : "";
    const paused = this.audio ? this.audio.paused : true;
    const isPlaying = (window as any).__isPlaying ?? false;
    const currentSongId = (window as any).__currentSongId ?? "";
    console.log(`[TRACE] Step: ${step}, TS: ${ts}ms, Func: ${funcName}, src: "${src}", paused: ${paused}, isPlaying: ${isPlaying}, currentSongId: "${currentSongId}"`);
  }

  private clearTransitionGuard() {
    this.isChangingSource = false;
    if (this.transitionTimeout) {
      clearTimeout(this.transitionTimeout);
      this.transitionTimeout = null;
    }
  }

  constructor() {
    if (typeof window === "undefined") return;

    this.audio = new Audio();
    this.audio.preload = "metadata";

    if (typeof window !== "undefined") {
      (window as any).__audio = this.audio;
    }

    const onTimeUpdate = () => this.handlers.onTimeUpdate?.(this.audio?.currentTime ?? 0);
    const onDurationChange = () => {
      const duration = Number.isFinite(this.audio?.duration) ? this.audio!.duration : 0;
      this.handlers.onDurationChange?.(duration);
    };
    const onPlay = () => {
      this.clearTransitionGuard();
      this.trace("BrowserPlaybackService.onPlay");
      this.log("onPlay", 47, { status: "play event fired, transition guard cleared" });
      this.handlers.onPlayStateChange?.(true);
      console.log("[HTMLAudioElement Event] play");
    };
    const onPause = () => {
      this.trace("BrowserPlaybackService.onPause");
      this.log("onPause", 51, { status: "pause event fired" });
      console.log("[HTMLAudioElement Event] pause");
      if (this.isChangingSource) {
        this.log("onPause", 53, { status: "pause event ignored (source transition in progress)" });
        return;
      }
      this.handlers.onPlayStateChange?.(false);
    };
    const onEnded = () => {
      this.trace("BrowserPlaybackService.onEnded");
      this.log("onEnded", 58, { status: "ended event fired" });
      console.log("[HTMLAudioElement Event] ended");
      this.handlers.onEnded?.();
    };
    const onError = () => {
      this.clearTransitionGuard();
      let msg = "Playback failed for the selected audio source";
      const errorObj = this.audio?.error;
      if (errorObj && errorObj.code === errorObj.MEDIA_ERR_SRC_NOT_SUPPORTED) {
        msg = "MEDIA_ERR_SRC_NOT_SUPPORTED";
      }
      this.log("onError", 67, { errorCode: errorObj?.code, errorMessage: msg });
      console.log("[HTMLAudioElement Event] error", {
        errorCode: errorObj?.code,
        networkState: this.audio?.networkState,
        errorMessage: msg
      });
      const error = new Error(msg);
      this.handlers.onError?.(error);
      this.handlers.onPlayStateChange?.(false);
    };

    const onPlaying = () => {
      this.clearTransitionGuard();
      this.trace("BrowserPlaybackService.onPlaying");
      this.log("onPlaying", 74, { status: "playing event fired, transition guard cleared" });
      console.log("[HTMLAudioElement Event] playing");
    };
    const onCanPlay = () => {
      this.clearTransitionGuard();
      this.log("onCanPlay", 78, { status: "canplay event fired, transition guard cleared" });
    };
    const onAbort = () => {
      this.clearTransitionGuard();
      this.log("onAbort", 82, { status: "abort event fired, transition guard cleared" });
    };
    const onEmptied = () => {
      this.log("onEmptied", 86, { status: "emptied event fired" });
    };
    const onWaiting = () => {
      this.log("onWaiting", 88, { status: "waiting event fired" });
      console.log("[HTMLAudioElement Event] waiting");
    };
    const onStalled = () => {
      console.log("[HTMLAudioElement Event] stalled");
    };

    this.audio.addEventListener("timeupdate", onTimeUpdate);
    this.audio.addEventListener("loadedmetadata", onDurationChange);
    this.audio.addEventListener("durationchange", onDurationChange);
    this.audio.addEventListener("play", onPlay);
    this.audio.addEventListener("pause", onPause);
    this.audio.addEventListener("ended", onEnded);
    this.audio.addEventListener("error", onError);
    this.audio.addEventListener("playing", onPlaying);
    this.audio.addEventListener("canplay", onCanPlay);
    this.audio.addEventListener("abort", onAbort);
    this.audio.addEventListener("emptied", onEmptied);
    this.audio.addEventListener("waiting", onWaiting);
    this.audio.addEventListener("stalled", onStalled);

    this.audio.onloadedmetadata = onDurationChange;
    this.audio.ondurationchange = onDurationChange;
  }

  setSource(source: string | null): void {
    const audio = this.audio;
    if (!audio) return;

    this.trace("BrowserPlaybackService.setSource");
    this.log("setSource", 112, { newSource: source });

    if (this.transitionTimeout) {
      clearTimeout(this.transitionTimeout);
      this.transitionTimeout = null;
    }

    if (!source) {
      this.clearTransitionGuard();
      audio.pause();
      audio.removeAttribute("src");
      audio.load();
      this.handlers.onTimeUpdate?.(0);
      this.handlers.onDurationChange?.(0);
      return;
    }

    this.isChangingSource = true;
    
    this.transitionTimeout = window.setTimeout(() => {
      if (this.isChangingSource) {
        this.log("transitionTimeout", 133, { status: "transition timeout fallback triggered, clearing flag" });
        this.isChangingSource = false;
      }
    }, 3500);

    this.currentSource = source;
    audio.src = source;
    this.log("setSource", 140, { status: "audio.src updated, calling audio.load()" });
    audio.load();
    this.handlers.onTimeUpdate?.(0);
  }

  async play(): Promise<void> {
    const audio = this.audio;
    if (!audio) return;

    this.trace("BrowserPlaybackService.play");
    this.log("play", 149, { status: "play() called" });
    try {
      this.log("play", 151, { status: "calling audio.play()" });
      await audio.play();
      this.log("play", 153, { status: "audio.play() promise resolved successfully" });
    } catch (error) {
      const playbackError = error instanceof Error ? error : new Error("Playback failed");
      
      if (playbackError.name === "AbortError" && this.isChangingSource) {
        this.log("play", 158, { status: "expected AbortError during source transition ignored" });
        this.clearTransitionGuard();
        return;
      }

      this.clearTransitionGuard();
      this.log("play", 164, { status: "audio.play() promise rejected", errorName: playbackError.name, errorMessage: playbackError.message });
      this.handlers.onError?.(playbackError);
      this.handlers.onPlayStateChange?.(false);
      throw playbackError;
    }
  }

  pause(): void {
    this.trace("BrowserPlaybackService.pause");
    this.log("pause", 172, { status: "pause() called" });
    this.clearTransitionGuard();
    this.audio?.pause();
  }

  seek(time: number): void {
    const audio = this.audio;
    if (!audio || !Number.isFinite(time)) return;
    const clamped = Math.max(0, time);
    this.log("seek", 181, { seekTarget: clamped });
    audio.currentTime = clamped;
    this.handlers.onTimeUpdate?.(clamped);
  }

  getCurrentTime(): number {
    return this.audio?.currentTime ?? 0;
  }

  getDuration(): number {
    if (!this.audio || !Number.isFinite(this.audio.duration)) return 0;
    return this.audio.duration;
  }

  isPaused(): boolean {
    return this.audio?.paused ?? true;
  }

  setEventHandlers(handlers: PlaybackEventHandlers): () => void {
    this.handlers = handlers;
    return () => {
      this.handlers = {};
    };
  }

  unlock(): void {
    const audio = this.audio;
    if (!audio) return;
    this.trace("BrowserPlaybackService.unlock");
    this.log("unlock", 208, { status: "unlock() called" });
    const playPromise = audio.play();
    if (playPromise !== undefined) {
      playPromise
        .then(() => {
          this.log("unlock", 213, { status: "unlock play succeeded, pausing" });
          audio.pause();
        })
        .catch((err) => {
          this.log("unlock", 216, { status: "unlock play rejected", error: err.message });
        });
    }
  }

  dispose(): void {
    const audio = this.audio;
    if (!audio) return;

    this.log("dispose", 225, { status: "dispose() called" });
    this.clearTransitionGuard();
    audio.pause();
    audio.src = "";
    audio.load();
    this.currentSource = null;
    this.audio = null;
    this.handlers = {};
  }

  getCurrentSource(): string | null {
    return this.currentSource;
  }

  resolveSource(song: Song): string | null {
    const audio = this.audio;
    const surroundUrl = song.surroundUrl?.trim() || null;
    const fallbackUrl = song.fallbackUrl?.trim() || null;
    const legacy = song.audioUrl?.trim() || null;

    // Enhanced logging: show all URL fields before selection
    console.log(`[resolveSource] Song: "${song.title}" (id: ${song.id})`);
    console.log(`[resolveSource]   surroundUrl: ${surroundUrl || '(null)'}`);
    console.log(`[resolveSource]   fallbackUrl: ${fallbackUrl || '(null)'}`);
    console.log(`[resolveSource]   audioUrl:    ${legacy || '(null)'}`);

    // If no surround URL exists, use fallback or legacy audioUrl
    if (!surroundUrl) {
      const selected = fallbackUrl || legacy;
      console.log(`[resolveSource] => Selected: ${selected || '(none)'} (type: ${fallbackUrl ? 'fallbackUrl' : legacy ? 'legacy audioUrl' : 'none'})`);
      return selected;
    }

    // Check if browser can play surround formats
    if (audio) {
      const canAC3 = audio.canPlayType('audio/mp4; codecs="ac-3"');
      const canEAC3 = audio.canPlayType('audio/mp4; codecs="ec-3"');
      const canDTS = audio.canPlayType('audio/vnd.dts');

      const surroundSupported = canAC3 === 'probably' || canAC3 === 'maybe'
        || canEAC3 === 'probably' || canEAC3 === 'maybe'
        || canDTS === 'probably' || canDTS === 'maybe';

      if (surroundSupported) {
        console.log(`[resolveSource] => Selected: ${surroundUrl} (type: surroundUrl — browser supports surround)`);
        this.log("resolveSource", 0, { status: "surround supported by browser", surroundUrl });
        return surroundUrl;
      }

      // If the surround file is not supported, automatically use fallbackUrl
      if (fallbackUrl) {
        console.log(`[resolveSource] => Selected: ${fallbackUrl} (type: fallbackUrl — surround not supported)`);
        this.log("resolveSource", 0, { status: "surround not supported, using fallback", fallbackUrl });
        return fallbackUrl;
      }
    }

    // No fallback available, return surround anyway (will trigger error handler)
    const finalUrl = surroundUrl || legacy;
    console.log(`[resolveSource] => Selected: ${finalUrl || '(none)'} (type: ${surroundUrl ? 'surroundUrl (no fallback)' : 'legacy audioUrl'})`);
    return finalUrl;
  }
}
