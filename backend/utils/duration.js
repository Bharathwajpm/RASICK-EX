const fs = require("fs");
const { parseFile } = require("music-metadata");

const formatDuration = (totalSeconds) => {
  if (!totalSeconds || !Number.isFinite(totalSeconds) || totalSeconds <= 0) {
    return "0:00";
  }

  const seconds = Math.max(1, Math.floor(totalSeconds));
  const minutes = Math.floor(seconds / 60);
  const remaining = seconds % 60;
  return `${minutes}:${remaining.toString().padStart(2, "0")}`;
};

function parseAc3Duration(buffer) {
  let totalSamples = 0;
  let sampleRate = 0;
  let i = 0;
  const len = buffer.length;

  while (i < len - 7) {
    if (buffer[i] === 0x0B && buffer[i + 1] === 0x77) {
      const bsid = (buffer[i + 5] & 0xF8) >> 3;
      if (bsid <= 16) {
        const fscod = (buffer[i + 4] & 0xC0) >> 6;
        let frameSampleRate = 0;
        if (fscod === 0) frameSampleRate = 48000;
        else if (fscod === 1) frameSampleRate = 44100;
        else if (fscod === 2) frameSampleRate = 32000;
        else frameSampleRate = 48000;

        if (frameSampleRate > 0) {
          sampleRate = frameSampleRate;
        }

        let samplesPerFrame = 1536;
        if (bsid >= 11 && bsid <= 16) {
          // EAC3 (Enhanced AC3 / Dolby Digital Plus)
          const numblkscod = (buffer[i + 4] & 0x18) >> 3;
          const blocks = numblkscod === 0 ? 1 : numblkscod === 1 ? 2 : numblkscod === 2 ? 3 : 6;
          samplesPerFrame = blocks * 256;
        }

        totalSamples += samplesPerFrame;
        i += 128; // Skip minimum frame size
        continue;
      }
    }
    i++;
  }

  if (totalSamples > 0 && sampleRate > 0) {
    return totalSamples / sampleRate;
  }
  return 0;
}

function parseDtsDuration(buffer) {
  let totalSamples = 0;
  let sampleRate = 0;
  let i = 0;
  const len = buffer.length;

  while (i < len - 12) {
    let isDts = false;
    let numBlocks = 0;
    let sampleRateCode = 0;

    if (buffer[i] === 0x7F && buffer[i + 1] === 0xFE && buffer[i + 2] === 0x80 && buffer[i + 3] === 0x01) {
      isDts = true;
      numBlocks = ((buffer[i + 4] & 0x01) << 6) | ((buffer[i + 5] & 0xFC) >> 2);
      sampleRateCode = (buffer[i + 8] & 0x3C) >> 2;
    } else if (buffer[i] === 0xFE && buffer[i + 1] === 0x7F && buffer[i + 2] === 0x01 && buffer[i + 3] === 0x80) {
      isDts = true;
      numBlocks = ((buffer[i + 5] & 0x01) << 6) | ((buffer[i + 4] & 0xFC) >> 2);
      sampleRateCode = (buffer[i + 9] & 0x3C) >> 2;
    }

    if (isDts) {
      let frameSampleRate = 0;
      switch (sampleRateCode) {
        case 1: frameSampleRate = 8000; break;
        case 2: frameSampleRate = 16000; break;
        case 3: frameSampleRate = 32000; break;
        case 6: frameSampleRate = 11025; break;
        case 7: frameSampleRate = 22050; break;
        case 8: frameSampleRate = 44100; break;
        case 11: frameSampleRate = 12000; break;
        case 12: frameSampleRate = 24000; break;
        case 13: frameSampleRate = 48000; break;
        default: frameSampleRate = 48000;
      }

      if (frameSampleRate > 0) {
        sampleRate = frameSampleRate;
      }

      const samplesPerFrame = (numBlocks + 1) * 32;
      totalSamples += samplesPerFrame;
      i += 512; // Skip minimum frame size
      continue;
    }
    i++;
  }

  if (totalSamples > 0 && sampleRate > 0) {
    return totalSamples / sampleRate;
  }
  return 0;
}

const getAudioDuration = async (filePath) => {
  try {
    // 1. Try music-metadata first
    const metadata = await parseFile(filePath);
    if (metadata.format.duration && metadata.format.duration > 0) {
      return formatDuration(metadata.format.duration);
    }
  } catch (err) {
    console.log(`[Duration Resolver] music-metadata failed for ${filePath}, falling back to custom binary parsing...`);
  }

  // 2. Fall back to manual binary parsing for AC3, EAC3, DTS, DTS-HD
  try {
    const buffer = await fs.promises.readFile(filePath);
    let seconds = parseAc3Duration(buffer);
    if (seconds <= 0) {
      seconds = parseDtsDuration(buffer);
    }

    if (seconds > 0) {
      console.log(`[Duration Resolver] Custom binary parser successfully calculated duration: ${seconds}s`);
      return formatDuration(seconds);
    }
  } catch (err) {
    console.error("[Duration Resolver] Custom binary parser failed:", err.message || err);
  }

  return "0:00";
};

module.exports = {
  formatDuration,
  getAudioDuration,
};
