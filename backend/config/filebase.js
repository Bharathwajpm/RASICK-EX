const { S3Client, PutObjectCommand, DeleteObjectCommand } = require("@aws-sdk/client-s3");
const fs = require("fs");

const s3Client = new S3Client({
  endpoint: process.env.FILEBASE_ENDPOINT || "https://s3.filebase.io",
  region: "us-east-1",
  credentials: {
    accessKeyId: process.env.FILEBASE_ACCESS_KEY,
    secretAccessKey: process.env.FILEBASE_SECRET_KEY,
  },
  forcePathStyle: true,
});

/**
 * Uploads a local file to Filebase S3 bucket using PutObjectCommand with progress logs and retries.
 * @param {string} localFilePath Path to the local file.
 * @param {string} destinationKey Key mapping in S3.
 * @param {string} contentType MIME type of the file.
 * @param {number} attempt Current retry index.
 * @returns {Promise<string>} Public Filebase S3 object URL.
 */
const uploadToFilebase = async (localFilePath, destinationKey, contentType, attempt = 1) => {
  let fileBuffer;
  try {
    const size = fs.statSync(localFilePath).size;
    console.log(`[Filebase S3] Read starting for key=${destinationKey} from path=${localFilePath} (${size} bytes)`);
    fileBuffer = await fs.promises.readFile(localFilePath);
  } catch (e) {
    console.error(`[Filebase S3] Failed to read local file: ${localFilePath}. Error: ${e.message}`);
    throw new Error(`Failed to read local file for upload: ${e.message}`);
  }

  const controller = new AbortController();
  const timeoutMs = 60000; // 60 seconds timeout
  const timeoutId = setTimeout(() => {
    console.warn(`[Filebase S3] Timeout (${timeoutMs}ms) exceeded for key=${destinationKey}. Aborting upload.`);
    controller.abort();
  }, timeoutMs);

  try {
    console.log(`[Filebase S3] Upload started: ${destinationKey} (attempt ${attempt})`);
    
    const command = new PutObjectCommand({
      Bucket: process.env.FILEBASE_BUCKET || "rasick-music",
      Key: destinationKey,
      Body: fileBuffer,
      ContentType: contentType,
    });

    await s3Client.send(command, { abortSignal: controller.signal });
    clearTimeout(timeoutId);

    console.log(`[Filebase S3] Upload completed successfully: ${destinationKey}`);

    const bucket = process.env.FILEBASE_BUCKET || "rasick-music";
    const cleanEndpoint = (process.env.FILEBASE_ENDPOINT || "https://s3.filebase.io")
      .replace(/^https?:\/\//, "")
      .replace(/\/$/, "");
    return `https://${bucket}.${cleanEndpoint}/${destinationKey}`;
  } catch (error) {
    clearTimeout(timeoutId);
    console.error(`[Filebase S3] Upload failed for ${destinationKey} (attempt ${attempt}): ${error.name} - ${error.message}`);
    
    if (attempt < 3) {
      const delay = Math.pow(2, attempt) * 1000;
      console.log(`[Filebase S3] Retrying upload in ${delay}ms...`);
      await new Promise((resolve) => setTimeout(resolve, delay));
      return uploadToFilebase(localFilePath, destinationKey, contentType, attempt + 1);
    }
    throw error;
  }
};

/**
 * Removes an object from Filebase S3 bucket.
 * @param {string} s3Url Public S3 URL or direct Key.
 * @returns {Promise<void>}
 */
const deleteFromFilebase = async (s3Url) => {
  if (!s3Url) return;
  
  let key = s3Url;
  try {
    const parsed = new URL(s3Url);
    // pathname starts with a slash, e.g. /audio/uuid.mp3
    key = parsed.pathname.substring(1);
    // If the path contains the bucket name (path-style resolution), remove it.
    const bucketName = process.env.FILEBASE_BUCKET || "rasick-music";
    if (key.startsWith(`${bucketName}/`)) {
      key = key.substring(bucketName.length + 1);
    }
  } catch (e) {
    // Treat as raw key if URL parsing fails
    key = s3Url.replace(/^\/?uploads\//, "");
  }

  try {
    console.log(`[Filebase S3] Deleting object key: ${key}`);
    const command = new DeleteObjectCommand({
      Bucket: process.env.FILEBASE_BUCKET || "rasick-music",
      Key: key,
    });
    await s3Client.send(command);
    console.log(`[Filebase S3] Object deleted successfully: ${key}`);
  } catch (error) {
    console.error(`[Filebase S3] Failed to delete object key ${key}: ${error.message}`);
    // Gracefully continue since object might have been deleted manually
  }
};

module.exports = {
  s3Client,
  uploadToFilebase,
  deleteFromFilebase,
};
