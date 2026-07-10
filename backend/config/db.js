const mongoose = require("mongoose");
const dns = require("dns");

const maskUri = (uri) => {
  if (!uri) return "undefined";
  return uri.replace(/(mongodb(?:\+srv)?:\/\/[^:]+:)([^@]+)(@.*)/, '$1******$3');
};

const connectDB = async () => {
  const uri = process.env.MONGODB_URI;

  if (!uri) {
    throw new Error("MONGODB_URI is not defined in environment variables");
  }

  console.log(`Loading MongoDB URI: ${maskUri(uri)}`);

  try {
    await mongoose.connect(uri, {
      serverSelectionTimeoutMS: 5000,
    });
    console.log("MongoDB connected successfully");
  } catch (error) {
    // Check if it's a DNS SRV resolution error
    if (error.message.includes("querySrv ECONNREFUSED") || error.message.includes("ENOTFOUND")) {
      console.warn("DNS resolution failed for MongoDB Atlas. Attempting fallback to public DNS (8.8.8.8, 1.1.1.1)...");
      try {
        dns.setServers(["8.8.8.8", "1.1.1.1"]);
        await mongoose.connect(uri, {
          serverSelectionTimeoutMS: 5000,
        });
        console.log("MongoDB connected successfully after applying DNS workaround");
        return;
      } catch (retryError) {
        handleConnectionError(retryError, uri);
      }
    } else {
      handleConnectionError(error, uri);
    }
  }
};

const handleConnectionError = (error, uri) => {
  console.error("Database connection failed details:", error.message);
  
  if (
    error.message.includes("Could not connect to any servers in your MongoDB Atlas cluster") ||
    error.message.includes("IP isn't whitelisted") ||
    error.message.includes("connection timeout") ||
    error.message.includes("connection closed")
  ) {
    throw new Error(
      "MongoDB Atlas connection blocked. This is likely an IP Whitelist (Network Access) issue in your MongoDB Atlas configuration. " +
      "Please log in to MongoDB Atlas and ensure that your current IP address (or 0.0.0.0/0 to allow all) is added to the IP Access List under 'Network Access'."
    );
  }
  
  throw error;
};

module.exports = connectDB;
