// utils/jwt.ts
import { decodeJwt, JWTPayload } from "jose";

export interface TokenPayload extends JWTPayload {
  sub: string;
  username?: string;
  email: string;
  role: string;
}

export function decodeToken(token: string): TokenPayload | null {
  try {
    return decodeJwt(token) as TokenPayload;
  } catch (err) {
    console.error("Invalid token", err);
    return null;
  }
}
