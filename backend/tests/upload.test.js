const test = require("node:test");
const assert = require("node:assert/strict");
const { isSupportedAudioFile } = require("../config/upload");

test("accepts mp3, dts, ac3, wav, aac, and flac files by extension or MIME type", () => {
  assert.equal(
    isSupportedAudioFile({ fieldname: "audio", originalname: "song.mp3", mimetype: "audio/mpeg" }),
    true,
  );
  assert.equal(
    isSupportedAudioFile({
      fieldname: "audio",
      originalname: "movie.dts",
      mimetype: "application/octet-stream",
    }),
    true,
  );
  assert.equal(
    isSupportedAudioFile({
      fieldname: "audio",
      originalname: "track.ac3",
      mimetype: "application/octet-stream",
    }),
    true,
  );
  assert.equal(
    isSupportedAudioFile({ fieldname: "audio", originalname: "song.wav", mimetype: "audio/wav" }),
    true,
  );
  assert.equal(
    isSupportedAudioFile({ fieldname: "audio", originalname: "song.flac", mimetype: "audio/flac" }),
    true,
  );
  assert.equal(
    isSupportedAudioFile({ fieldname: "audio", originalname: "song.aac", mimetype: "audio/aac" }),
    true,
  );
  assert.equal(
    isSupportedAudioFile({ fieldname: "audio", originalname: "song.ogg", mimetype: "audio/ogg" }),
    false,
  );
});
