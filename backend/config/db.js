const mongoose = require("mongoose");
const dns = require("dns").promises;
const net = require("net");

const maskUri = (uri) => {
  if (!uri) return "undefined";
  return uri.replace(/(mongodb(?:\+srv)?:\/\/[^:]+:)([^@]+)(@.*)/, '$1******$3');
};

const runDiagnostics = async (uri) => {
  console.log("\n=== MongoDB Atlas Startup Diagnostics ===");
  console.log(`Node.js Version: ${process.version}`);
  console.log(`Mongoose Version: ${mongoose.version}`);
  
  // Extract hostname from URI
  let host = "";
  try {
    const match = uri.match(/@([^/?#]+)/);
    if (match) {
      host = match[1];
    }
  } catch (err) {
    console.error("Failed to parse hostname from URI:", err.message);
  }

  if (!host) {
    console.error("Could not extract host from URI.");
    return;
  }

  console.log(`Parsed Host: ${host}`);

  // 1. DNS Resolution Check
  console.log("\n1. DNS Resolution Diagnostics:");
  let srvHosts = [];
  try {
    console.log(`Resolving SRV records for '_mongodb._tcp.${host}'...`);
    const srvRecords = await dns.resolveSrv(`_mongodb._tcp.${host}`);
    console.log("✓ SRV Records resolved successfully:");
    console.log(JSON.stringify(srvRecords, null, 2));
    srvHosts = srvRecords.map(r => ({ name: r.name, port: r.port }));
  } catch (dnsErr) {
    console.error(`✗ DNS SRV Lookup Failed: ${dnsErr.message}`);
    console.error(`Error Code: ${dnsErr.code}`);
    console.error("\nThis is likely a DNS configuration issue. If you are on Windows:");
    console.error("- Flush your DNS cache: open Command Prompt as Administrator and run: ipconfig /flushdns");
    console.error("- Change your Windows Network DNS server configuration to Google Public DNS (8.8.8.8, 8.8.4.4) or Cloudflare (1.1.1.1).");
  }

  // 2. Resolve Shard host IP addresses and check TCP connectivity
  if (srvHosts.length > 0) {
    console.log("\n2. Network TCP & Firewall Diagnostics:");
    for (const hostInfo of srvHosts) {
      console.log(`\nChecking host: ${hostInfo.name}`);
      
      // Resolve A records
      let resolvedIps = [];
      try {
        resolvedIps = await dns.resolve4(hostInfo.name);
        console.log(`  ✓ Resolved to IPs: ${resolvedIps.join(", ")}`);
      } catch (ipErr) {
        console.error(`  ✗ DNS A Record Lookup Failed for ${hostInfo.name}: ${ipErr.message}`);
        continue;
      }
      
      // TCP connection test
      for (const ip of resolvedIps) {
        console.log(`  Connecting to ${ip}:${hostInfo.port} via TCP socket...`);
        const status = await testTcpConnection(ip, hostInfo.port);
        if (status.success) {
          console.log(`  ✓ TCP Connection to ${ip}:${hostInfo.port} SUCCESSFUL.`);
        } else {
          console.error(`  ✗ TCP Connection to ${ip}:${hostInfo.port} FAILED.`);
          console.error(`    Error: ${status.error}`);
          console.error(`    This indicates that outbound traffic on port ${hostInfo.port} is blocked by a firewall, antivirus, proxy, or ISP, or the MongoDB Atlas cluster IP whitelist configuration is blocking this connection.`);
        }
      }
    }
  } else {
    console.log("\n2. Network TCP & Firewall Diagnostics skipped due to DNS resolution failure.");
  }
  console.log("=========================================\n");
};

const testTcpConnection = (host, port) => {
  return new Promise((resolve) => {
    const socket = new net.Socket();
    const timeout = 3000;
    
    socket.setTimeout(timeout);
    
    socket.on("connect", () => {
      socket.destroy();
      resolve({ success: true });
    });
    
    socket.on("timeout", () => {
      socket.destroy();
      resolve({ success: false, error: `Connection timed out after ${timeout}ms` });
    });
    
    socket.on("error", (err) => {
      socket.destroy();
      resolve({ success: false, error: err.message });
    });
    
    socket.connect(port, host);
  });
};

const connectDB = async () => {
  const uri = process.env.MONGODB_URI;

  if (!uri) {
    throw new Error("MONGODB_URI is not defined in environment variables");
  }

  const isProduction = process.env.NODE_ENV === "production";

  if (!isProduction) {
    console.log(`Loading MongoDB URI: ${maskUri(uri)}`);
  } else {
    console.log("Connecting to MongoDB Atlas...");
  }

  try {
    await mongoose.connect(uri, {
      serverSelectionTimeoutMS: 5000,
    });
    console.log("MongoDB connected successfully");
  } catch (error) {
    console.error("\n================ DATABASE CONNECTION FAILED ================");
    console.error(`Error Name:    ${error.name}`);
    console.error(`Error Code:    ${error.code || "N/A"}`);
    console.error(`Error Message: ${error.message}`);
    
    if (!isProduction) {
      if (error.reason) {
        console.error(`Error Reason:  ${JSON.stringify(error.reason, null, 2)}`);
      }
      if (error.cause) {
        console.error(`Error Cause:   ${JSON.stringify(error.cause, null, 2)}`);
      }
      
      console.error("\nStack Trace:");
      console.error(error.stack);
    }
    console.error("============================================================\n");

    // Run connection diagnostics only in development
    if (!isProduction) {
      try {
        await runDiagnostics(uri);
      } catch (diagErr) {
        console.error("Diagnostics error:", diagErr.message);
      }
    }

    throw error;
  }
};

module.exports = connectDB;
