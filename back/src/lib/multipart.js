const path = require("path");
const fs = require("fs/promises");
const { randomUUID } = require("crypto");

const MAX_BODY_SIZE = 10 * 1024 * 1024; // 10MB
const ALLOWED_IMAGE_MIME = new Set([
  "image/png",
  "image/jpeg",
  "image/jpg",
  "image/webp",
  "image/gif",
  "image/svg+xml",
]);

function extractBoundary(contentType) {
  const boundaryKey = "boundary=";
  const index = contentType.indexOf(boundaryKey);
  if (index === -1) return null;
  return contentType.slice(index + boundaryKey.length).trim();
}

function normalizeFieldName(value) {
  return String(value || "").trim().toLowerCase();
}

function extFromFilename(filename) {
  const ext = path.extname(filename || "").toLowerCase();
  if (ext && ext.length <= 8) return ext;
  return "";
}

function extFromMime(mime) {
  switch (mime) {
    case "image/png":
      return ".png";
    case "image/jpeg":
    case "image/jpg":
      return ".jpg";
    case "image/webp":
      return ".webp";
    case "image/gif":
      return ".gif";
    case "image/svg+xml":
      return ".svg";
    default:
      return ".bin";
  }
}

async function parseMultipartFormData(req) {
  const contentType = String(req.headers["content-type"] || "");
  if (!contentType.startsWith("multipart/form-data")) {
    return {
      error: "Content-Type must be multipart/form-data.",
      status: 400,
    };
  }

  const boundary = extractBoundary(contentType);
  if (!boundary) {
    return {
      error: "Invalid multipart boundary.",
      status: 400,
    };
  }

  const chunks = [];
  let total = 0;

  for await (const chunk of req) {
    total += chunk.length;
    if (total > MAX_BODY_SIZE) {
      return {
        error: "Uploaded payload is too large (max 10MB).",
        status: 413,
      };
    }
    chunks.push(chunk);
  }

  const raw = Buffer.concat(chunks);
  const rawStr = raw.toString("latin1");
  const delimiter = `--${boundary}`;
  const parts = rawStr.split(delimiter).slice(1, -1);

  const fields = {};
  const files = {};

  for (const partRaw of parts) {
    let part = partRaw;
    if (part.startsWith("\r\n")) {
      part = part.slice(2);
    }
    if (part.endsWith("\r\n")) {
      part = part.slice(0, -2);
    }
    const splitIndex = part.indexOf("\r\n\r\n");
    if (splitIndex === -1) continue;

    const headerText = part.slice(0, splitIndex);
    let bodyText = part.slice(splitIndex + 4);
    if (bodyText.endsWith("\r\n")) {
      bodyText = bodyText.slice(0, -2);
    }

    const headerLines = headerText.split("\r\n");
    const dispositionLine =
      headerLines.find((line) =>
        line.toLowerCase().startsWith("content-disposition:")
      ) || "";
    const typeLine =
      headerLines.find((line) => line.toLowerCase().startsWith("content-type:")) ||
      "";

    const nameMatch = dispositionLine.match(/name="([^"]+)"/i);
    if (!nameMatch) continue;
    const fieldName = normalizeFieldName(nameMatch[1]);

    const fileNameMatch = dispositionLine.match(/filename="([^"]*)"/i);
    if (!fileNameMatch) {
      fields[fieldName] = Buffer.from(bodyText, "latin1").toString("utf8").trim();
      continue;
    }

    const originalFilename = fileNameMatch[1] || "";
    const mimeType = typeLine.split(":")[1]?.trim().toLowerCase() || "";
    const buffer = Buffer.from(bodyText, "latin1");
    files[fieldName] = {
      originalFilename,
      mimeType,
      buffer,
    };
  }

  return { fields, files };
}

async function saveImageFile(file, destinationDir, prefix) {
  if (!file || !Buffer.isBuffer(file.buffer) || file.buffer.length === 0) {
    return {
      error: `${prefix} file is required.`,
      status: 400,
    };
  }

  if (!ALLOWED_IMAGE_MIME.has(file.mimeType)) {
    return {
      error: `${prefix} must be a valid image (png, jpg, webp, gif, svg).`,
      status: 400,
    };
  }

  const ext = extFromFilename(file.originalFilename) || extFromMime(file.mimeType);
  const filename = `${prefix}-${randomUUID()}${ext}`;
  const absolutePath = path.join(destinationDir, filename);
  await fs.mkdir(destinationDir, { recursive: true });
  await fs.writeFile(absolutePath, file.buffer);
  return { filename };
}

async function safeDeleteFile(absolutePath) {
  if (!absolutePath) return;
  try {
    await fs.unlink(absolutePath);
  } catch {
    // ignore cleanup errors
  }
}

module.exports = {
  parseMultipartFormData,
  saveImageFile,
  safeDeleteFile,
};

