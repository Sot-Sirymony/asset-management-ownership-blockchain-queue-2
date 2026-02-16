"use client";
import jwt from "jsonwebtoken";

const getUserRoleFromToken = (token) => {
  if (!token) return undefined;
  const decodedToken = jwt.decode(token);
  return decodedToken?.role;
};

export default getUserRoleFromToken;
