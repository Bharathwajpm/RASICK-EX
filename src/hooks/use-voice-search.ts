import { useCallback, useEffect, useRef, useState } from "react";

type SpeechRecognitionCtor = new () => SpeechRecognition;

function getSpeechRecognition(): SpeechRecognitionCtor | null {
  if (typeof window === "undefined") return null;
  const w = window as Window & {
    SpeechRecognition?: SpeechRecognitionCtor;
    webkitSpeechRecognition?: SpeechRecognitionCtor;
  };
  return w.SpeechRecognition ?? w.webkitSpeechRecognition ?? null;
}

export type VoiceSearchStatus =
  | "idle"
  | "listening"
  | "processing"
  | "unsupported"
  | "no-speech"
  | "error";

export function useVoiceSearch(onResult: (text: string) => void) {
  const [status, setStatus] = useState<VoiceSearchStatus>("idle");
  const [message, setMessage] = useState("");
  const recognitionRef = useRef<SpeechRecognition | null>(null);
  const gotResultRef = useRef(false);

  const isSupported = typeof window !== "undefined" && getSpeechRecognition() !== null;

  const stopListening = useCallback(() => {
    recognitionRef.current?.stop();
    recognitionRef.current = null;
  }, []);

  const startListening = useCallback(() => {
    const SpeechRecognitionClass = getSpeechRecognition();
    if (!SpeechRecognitionClass) {
      setStatus("unsupported");
      setMessage("Voice search is not supported in this browser");
      return;
    }

    stopListening();
    gotResultRef.current = false;

    const recognition = new SpeechRecognitionClass();
    recognition.lang = "en-IN";
    recognition.interimResults = false;
    recognition.maxAlternatives = 1;
    recognition.continuous = false;

    recognition.onstart = () => {
      setStatus("listening");
      setMessage("Listening...");
    };

    recognition.onresult = (event: SpeechRecognitionEvent) => {
      gotResultRef.current = true;
      setStatus("processing");
      setMessage("Processing...");
      const transcript = event.results[0]?.[0]?.transcript?.trim() ?? "";
      if (transcript) {
        onResult(transcript);
        setStatus("idle");
        setMessage("");
      } else {
        setStatus("no-speech");
        setMessage("No speech detected");
      }
    };

    recognition.onerror = (event: SpeechRecognitionErrorEvent) => {
      if (event.error === "no-speech") {
        setStatus("no-speech");
        setMessage("No speech detected");
        return;
      }
      if (event.error === "not-allowed" || event.error === "service-not-allowed") {
        setStatus("error");
        setMessage("Microphone permission denied");
        return;
      }
      setStatus("error");
      setMessage("Voice search failed. Please try again.");
    };

    recognition.onend = () => {
      recognitionRef.current = null;
      if (!gotResultRef.current) {
        setStatus("no-speech");
        setMessage("No speech detected");
      }
    };

    recognitionRef.current = recognition;

    try {
      recognition.start();
    } catch {
      setStatus("error");
      setMessage("Could not start voice search");
    }
  }, [onResult, stopListening]);

  useEffect(() => () => stopListening(), [stopListening]);

  const clearMessage = useCallback(() => {
    setStatus("idle");
    setMessage("");
  }, []);

  return {
    isSupported,
    status,
    message,
    startListening,
    stopListening,
    clearMessage,
  };
}
