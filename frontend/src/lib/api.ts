const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1";

async function request<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const token = localStorage.getItem("jwt_token");
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(options.headers as Record<string, string>),
  };
  if (token) headers["Authorization"] = `Bearer ${token}`;

  const res = await fetch(`${API_BASE}${endpoint}`, { ...options, headers });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `Request failed (${res.status})`);
  }

  const contentType = res.headers.get("content-type");
  if (contentType?.includes("application/json")) {
    return res.json();
  }
  return res.text() as unknown as T;
}

// Auth
export interface RegisterUserDto {
  email: string;
  password: string;
  username: string;
}
export interface LoginUserDto {
  email: string;
  password: string;
}
export interface VerifyUserDto {
  email: string;
  verificationCode: string;
}
export interface LoginResponse {
  token: string;
  expiresIn: number;
}
export interface User {
  id: number;
  username: string;
  email: string;
  enabled: boolean;
}

export const authApi = {
  signup: (data: RegisterUserDto) =>
    request<User>("/auth/signup", { method: "POST", body: JSON.stringify(data) }),
  login: (data: LoginUserDto) =>
    request<LoginResponse>("/auth/login", { method: "POST", body: JSON.stringify(data) }),
  verify: (data: VerifyUserDto) =>
    request<string>("/auth/verify", { method: "POST", body: JSON.stringify(data) }),
  resend: (email: string) =>
    request<string>("/auth/resend", { method: "POST", body: JSON.stringify(email) }),
};

export const userApi = {
  me: () => request<User>("/users/me"),
};

// Collection
export type ReadingStatus = "COMPLETED" | "IN_PROGRESS" | "PLAN_TO_READ" | "DROPPED";

export interface Book {
  id?: number;
  openLibraryId: string;
  title: string;
  authorName: string;
  coverId?: number;
  isbn?: string;
  firstSentence?: string;
  description?: string;
}

export interface UserBookDto {
  book: Book;
  readingStatus: ReadingStatus;
  rating?: number;
  review?: string;
}

export interface UserBook {
  id: number;
  book: Book;
  readingStatus: ReadingStatus;
  rating?: number;
  review?: string;
}

export const collectionApi = {
  getAll: () => request<UserBook[]>("/userCollection"),
  add: (data: UserBookDto) =>
    request<UserBook>("/userCollection", { method: "POST", body: JSON.stringify(data) }),
  update: (openLibraryId: string, data: UserBookDto) =>
    request<UserBook>(`/userCollection/${openLibraryId}`, { method: "PATCH", body: JSON.stringify(data) }),
  remove: (openLibraryId: string) =>
    request<string>(`/userCollection/${openLibraryId}`, { method: "DELETE" }),
};
