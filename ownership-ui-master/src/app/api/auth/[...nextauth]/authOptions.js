import NextAuth from "next-auth";
import CredentialsProvider from "next-auth/providers/credentials";
import { loginService } from "../../../components/service/auth.service";
import jwt from "jsonwebtoken";

export const authOptions = {
  debug: true, // ✅ show NextAuth logs in terminal

  providers: [
    CredentialsProvider({
      name: "Credentials",
      credentials: {
        username: { label: "Username", type: "text" },
        password: { label: "Password", type: "password" },
      },

      async authorize(credentials) {
        // ✅ validate inputs
        if (!credentials?.username || !credentials?.password) return null;

        try {
          // ✅ call backend login
          const tokenResponse = await loginService({
            username: credentials.username,
            password: credentials.password,
          });

          // IMPORTANT: adjust this line to match your real response
          const token =
            tokenResponse?.payload?.token ||
            tokenResponse?.token ||
            tokenResponse?.accessToken ||
            tokenResponse?.data?.token;

          if (!token) {
            console.error("Login failed: no token in response", tokenResponse);
            return null; // => NextAuth returns 401
          }

          const decoded = jwt.decode(token);

          if (!decoded || typeof decoded !== "object") {
            console.error("Failed to decode token:", token);
            return null;
          }

          // ✅ adapt claim names (common JWTs use sub, roles, id)
          const userId = decoded.userId ?? decoded.id ?? decoded.user_id ?? decoded.sub;
          const role = decoded.role ?? decoded.roles ?? decoded.authorities ?? "USER";
          const name = decoded.username ?? decoded.sub ?? credentials.username;

          return {
            id: String(userId),
            name: String(name),
            role,
            accessToken: token,
          };
        } catch (err) {
          console.error("Auth error:", err);
          return null;
        }
      },
    }),
  ],

  callbacks: {
    async jwt({ token, user }) {
      if (user) {
        token.accessToken = user.accessToken;
        token.role = user.role;
        token.userId = user.id;
      }
      return token;
    },

    async session({ session, token }) {
      session.accessToken = token.accessToken;
      session.user = session.user || {};
      session.user.role = token.role;
      session.user.id = token.userId;
      return session;
    },
  },

  pages: {
    signIn: "/admin/login", // ✅ use your real login page
    error: "/auth/error",
  },

  session: {
    strategy: "jwt",
    maxAge: 24 * 60 * 60,
  },
};

export default NextAuth(authOptions);
