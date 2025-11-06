export const AUTH_BASE =
  process.env.NEXT_PUBLIC_AUTH_URL || "http://localhost:8081";

export const CUSTOMER_BASE =
  process.env.NEXT_PUBLIC_CUSTOMER_URL || "http://localhost:8082";

// Generic fetch with token + JSON handling
function makeApi(base: string) {
  return async function api(path: string, options: RequestInit = {}) {
    const token =
      typeof window !== "undefined" ? localStorage.getItem("token") : null;

    const headers = new Headers(options.headers || {});
    if (options.body && !headers.has("Content-Type")) {
      headers.set("Content-Type", "application/json");
    }
    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
      if (typeof window !== "undefined") {
        console.log("[FE] attaching auth", `Bearer ${token.slice(0, 20)}...`);
      }
    }

    const res = await fetch(`${base}${path}`, {
      ...options,
      headers,
      credentials: "omit",
    });

    if (res.status === 204) return null;

    const text = await res.text().catch(() => "");
    const ct = res.headers.get("content-type") || "";
    const isJson = ct.includes("application/json");

    if (res.ok) return isJson && text ? JSON.parse(text) : text;

    // Force relogin on 401/403 (and 500 from auth'd calls)
    const shouldForceRelogin = res.status === 401 || res.status === 403;

    if (shouldForceRelogin) {
      try { localStorage.removeItem("token"); } catch {}
      if (typeof window !== "undefined") {
        window.location.href = "/login?reason=sessionExpired";
      }
    }

    throw new Error(`${res.status} ${res.statusText}${text ? ` - ${text}` : ""}`);
  };
}

// Use these in your pages/components:
export const authApi = makeApi(AUTH_BASE);
export const customerApi = makeApi(CUSTOMER_BASE);
